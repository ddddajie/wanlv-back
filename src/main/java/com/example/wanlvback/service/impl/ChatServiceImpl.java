package com.example.wanlvback.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.example.wanlvback.constant.MessageConstant;
import com.example.wanlvback.exception.BaseException;
import com.example.wanlvback.mapper.SysNormalUserMapper;
import com.example.wanlvback.mapper.VisitorMessageMapper;
import com.example.wanlvback.mapper.VisitorSessionMapper;
import com.example.wanlvback.pojo.dto.ChatAskDTO;
import com.example.wanlvback.pojo.dto.SessionAnalysisTriggerDTO;
import com.example.wanlvback.pojo.dto.SessionScenicAreaBindDTO;
import com.example.wanlvback.pojo.entity.SysNormalUser;
import com.example.wanlvback.pojo.entity.VisitorMessage;
import com.example.wanlvback.pojo.entity.VisitorSession;
import com.example.wanlvback.pojo.vo.AgentChatResponseVO;
import com.example.wanlvback.pojo.vo.AgentSessionAnalysisResponseVO;
import com.example.wanlvback.pojo.vo.AgentSessionAnalysisVO;
import com.example.wanlvback.pojo.vo.ChatAnswerVO;
import com.example.wanlvback.pojo.vo.SessionAnalysisBatchItemVO;
import com.example.wanlvback.pojo.vo.SessionAnalysisBatchVO;
import com.example.wanlvback.service.ChatService;
import com.example.wanlvback.service.UserDigitalProfileService;
import com.example.wanlvback.utils.AgentChatHttpUtil;
import com.example.wanlvback.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聊天业务实现类。
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    private static final String SESSION_TYPE_CONSULTATION = "CONSULTATION";
    private static final String SESSION_TYPE_SCENIC_SERVICE = "SCENIC_SERVICE";
    private static final String SESSION_STATUS_ACTIVE = "ACTIVE";
    private static final String SESSION_STATUS_ANALYZED = "ANALYZED";
    private static final String SCENIC_AREA_SOURCE_UNSET = "UNSET";
    private static final String SCENIC_AREA_SOURCE_FRONTEND = "FRONTEND";
    /**
     * 业务会话编码格式：session_yyyyMMdd_userId。
     */
    private static final DateTimeFormatter SESSION_CODE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private VisitorSessionMapper visitorSessionMapper;

    @Autowired
    private VisitorMessageMapper visitorMessageMapper;

    @Autowired
    private SysNormalUserMapper sysNormalUserMapper;

    @Autowired
    private AgentChatHttpUtil agentChatHttpUtil;

    @Autowired
    private UserDigitalProfileService userDigitalProfileService;

    /**
     * 处理聊天请求。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatAnswerVO ask(ChatAskDTO chatAskDTO) {
        validateChatAskDTO(chatAskDTO);

        LocalDate reportDate = LocalDate.now();
        SysNormalUser normalUser = getNormalUser(chatAskDTO.getUserId());
        VisitorSession visitorSession = getOrCreateSession(chatAskDTO, normalUser, reportDate);
        visitorSession = refreshScenicAreaInfoIfNecessary(visitorSession, chatAskDTO);

        String askContent = resolveAskContent(chatAskDTO);

        /*
         * 先保存用户消息，再调用 Agent。
         * 这样日报总结时就能稳定通过 session_id 拉到完整消息链路。
         */
        saveMessage(
                visitorSession,
                chatAskDTO.getUserId(),
                "visitor",
                defaultMessageType(chatAskDTO.getMessageType()),
                StringUtils.hasText(chatAskDTO.getContent()) ? chatAskDTO.getContent() : askContent,
                chatAskDTO.getVoiceText()
        );

        AgentChatResponseVO agentResponse = agentChatHttpUtil.requestChat(visitorSession, normalUser, chatAskDTO, askContent);

        /*
         * 保存 Agent 回复，保证 visitor_message 中一问一答完整可追溯。
         */
        saveMessage(visitorSession, chatAskDTO.getUserId(), "agent", "text", agentResponse.getResponse(), null);

        return ChatAnswerVO.builder()
                .answer(agentResponse.getResponse())
                .sessionId(visitorSession.getId())
                .sessionCode(visitorSession.getSessionCode())
                .reportDate(visitorSession.getReportDate())
                .sessionType(resolveResponseSessionType(visitorSession, agentResponse))
                .scenicAreaId(visitorSession.getScenicAreaId())
                .scenicAreaConfirmed(visitorSession.getScenicAreaConfirmed())
                .detectedScenicAreaId(agentResponse.getDetectedScenicAreaId())
                .detectedScenicAreaName(agentResponse.getDetectedScenicAreaName())
                .detectionConfidence(agentResponse.getDetectionConfidence())
                .needScenicAreaConfirm(Boolean.TRUE.equals(agentResponse.getNeedScenicAreaConfirm()))
                .build();
    }

    /**
     * 手动触发单个用户某一天的日报总结。
     * 该入口要求超级管理员身份。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentSessionAnalysisVO analyzeSession(SessionAnalysisTriggerDTO triggerDTO) {
        validateSuperAdminOperator(triggerDTO);
        if (triggerDTO.getUserId() == null) {
            throw new BaseException(MessageConstant.USER_ID_REQUIRED);
        }

        LocalDate reportDate = resolveReportDate(triggerDTO.getReportDate());
        VisitorSession visitorSession = visitorSessionMapper.getByUserIdAndReportDate(triggerDTO.getUserId(), reportDate);
        if (visitorSession == null) {
            throw new BaseException(MessageConstant.SESSION_NOT_FOUND);
        }

        return analyzeSingleSession(visitorSession, Boolean.TRUE.equals(triggerDTO.getForceReanalyze()));
    }

    /**
     * 管理员手动触发某一天的批量日报总结。
     */
    @Override
    public SessionAnalysisBatchVO analyzeDailySessions(SessionAnalysisTriggerDTO triggerDTO) {
        validateSuperAdminOperator(triggerDTO);
        return analyzeDailySessions(resolveReportDate(triggerDTO.getReportDate()),
                Boolean.TRUE.equals(triggerDTO.getForceReanalyze()));
    }

    /**
     * 定时任务批量触发某一天的日报总结。
     * 这里不做管理员校验，供系统内部调用。
     */
    @Override
    public SessionAnalysisBatchVO analyzeDailySessions(LocalDate reportDate, boolean forceReanalyze) {
        LocalDate targetDate = resolveReportDate(reportDate);
        List<VisitorSession> sessionList = visitorSessionMapper.listByReportDate(targetDate);
        List<SessionAnalysisBatchItemVO> items = new ArrayList<>();

        if (sessionList == null || sessionList.isEmpty()) {
            return SessionAnalysisBatchVO.builder()
                    .reportDate(targetDate)
                    .totalCount(0)
                    .successCount(0)
                    .skippedCount(0)
                    .failedCount(0)
                    .items(items)
                    .build();
        }

        int successCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        for (VisitorSession visitorSession : sessionList) {
            try {
                if (isSessionAlreadyAnalyzed(visitorSession) && !forceReanalyze) {
                    skippedCount++;
                    items.add(SessionAnalysisBatchItemVO.builder()
                            .sessionId(visitorSession.getId())
                            .sessionCode(visitorSession.getSessionCode())
                            .userId(visitorSession.getUserId())
                            .success(Boolean.TRUE)
                            .skipped(Boolean.TRUE)
                            .message(MessageConstant.SESSION_ALREADY_ANALYZED)
                            .summary(visitorSession.getSummary())
                            .build());
                    continue;
                }

                AgentSessionAnalysisVO analysisVO = analyzeSingleSession(visitorSession, true);
                successCount++;
                items.add(SessionAnalysisBatchItemVO.builder()
                        .sessionId(visitorSession.getId())
                        .sessionCode(visitorSession.getSessionCode())
                        .userId(visitorSession.getUserId())
                        .success(Boolean.TRUE)
                        .skipped(Boolean.FALSE)
                        .message(MessageConstant.SESSION_ANALYZE_SUCCESS)
                        .summary(analysisVO.getSummary())
                        .build());
            } catch (Exception ex) {
                failedCount++;
                log.error("生成会话日报总结失败，sessionId={}, sessionCode={}, reportDate={}",
                        visitorSession.getId(), visitorSession.getSessionCode(), targetDate, ex);
                items.add(SessionAnalysisBatchItemVO.builder()
                        .sessionId(visitorSession.getId())
                        .sessionCode(visitorSession.getSessionCode())
                        .userId(visitorSession.getUserId())
                        .success(Boolean.FALSE)
                        .skipped(Boolean.FALSE)
                        .message(ex.getMessage())
                        .summary(null)
                        .build());
            }
        }

        return SessionAnalysisBatchVO.builder()
                .reportDate(targetDate)
                .totalCount(sessionList.size())
                .successCount(successCount)
                .skippedCount(skippedCount)
                .failedCount(failedCount)
                .items(items)
                .build();
    }

    /**
     * 绑定当前用户当天会话的景区信息。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long bindScenicArea(SessionScenicAreaBindDTO bindDTO) {
        if (bindDTO == null || bindDTO.getUserId() == null) {
            throw new BaseException(MessageConstant.USER_ID_REQUIRED);
        }
        if (bindDTO.getScenicAreaId() == null) {
            throw new BaseException(MessageConstant.SCENIC_AREA_ID_REQUIRED);
        }

        VisitorSession visitorSession = visitorSessionMapper.getByUserIdAndReportDate(bindDTO.getUserId(), LocalDate.now());
        if (visitorSession == null) {
            throw new BaseException(MessageConstant.SCENIC_SESSION_NOT_FOUND);
        }

        VisitorSession updateSession = VisitorSession.builder()
                .id(visitorSession.getId())
                .scenicAreaId(bindDTO.getScenicAreaId())
                .scenicAreaSource(defaultIfBlank(bindDTO.getScenicAreaSource(), SCENIC_AREA_SOURCE_FRONTEND))
                .scenicAreaConfirmed(bindDTO.getScenicAreaConfirmed() != null ? bindDTO.getScenicAreaConfirmed() : 1)
                .sessionType(defaultIfBlank(bindDTO.getSessionType(), SESSION_TYPE_SCENIC_SERVICE))
                .build();
        visitorSessionMapper.updateScenicAreaInfo(updateSession);
        return visitorSession.getId();
    }

    /**
     * 单条会话日报生成核心逻辑。
     * 会话通过 visitor_session.id 关联 visitor_message.session_id。
     */
    private AgentSessionAnalysisVO analyzeSingleSession(VisitorSession visitorSession, boolean forceReanalyze) {
        if (visitorSession == null) {
            throw new BaseException(MessageConstant.SESSION_EMPTY);
        }

        if (isSessionAlreadyAnalyzed(visitorSession) && !forceReanalyze) {
            return buildAnalysisVOFromSession(visitorSession);
        }

        List<VisitorMessage> messageList = visitorMessageMapper.listBySessionId(visitorSession.getId());
        if (messageList == null || messageList.isEmpty()) {
            throw new BaseException(MessageConstant.SESSION_MESSAGE_EMPTY);
        }

        AgentSessionAnalysisResponseVO responseVO = agentChatHttpUtil.requestSessionAnalysis(visitorSession, messageList);
        AgentSessionAnalysisVO analysisVO = responseVO.getAnalysis();
        if (analysisVO == null) {
            throw new BaseException(MessageConstant.AGENT_ANALYSIS_EMPTY);
        }

        saveAnalysisResult(visitorSession, messageList, analysisVO);
        return analysisVO;
    }

    /**
     * 将日报分析结果回写到 visitor_session。
     */
    private void saveAnalysisResult(VisitorSession visitorSession, List<VisitorMessage> messageList,
                                    AgentSessionAnalysisVO analysisVO) {
        VisitorSession updateSession = VisitorSession.builder()
                .id(visitorSession.getId())
                .sessionStatus(SESSION_STATUS_ANALYZED)
                .interactionCount(countUserInteraction(messageList))
                .summary(analysisVO.getSummary())
                .overallSentiment(analysisVO.getOverallSentiment())
                .sentimentScore(analysisVO.getSentimentScore())
                .focusTopics(JSON.toJSONString(analysisVO.getFocusTopics()))
                .interestTags(JSON.toJSONString(analysisVO.getInterestTags()))
                .serviceSuggestions(JSON.toJSONString(analysisVO.getServiceSuggestions()))
                .knowledgeGapPoints(JSON.toJSONString(analysisVO.getKnowledgeGapPoints()))
                .build();
        visitorSessionMapper.updateAnalysisResult(updateSession);

        /*
         * 同步更新内存对象。
         * 这样批量任务后续判断“是否已生成日报”时不需要重新查库。
         */
        visitorSession.setSessionStatus(updateSession.getSessionStatus());
        visitorSession.setInteractionCount(updateSession.getInteractionCount());
        visitorSession.setSummary(updateSession.getSummary());
        visitorSession.setOverallSentiment(updateSession.getOverallSentiment());
        visitorSession.setSentimentScore(updateSession.getSentimentScore());
        visitorSession.setFocusTopics(updateSession.getFocusTopics());
        visitorSession.setInterestTags(updateSession.getInterestTags());
        visitorSession.setServiceSuggestions(updateSession.getServiceSuggestions());
        visitorSession.setKnowledgeGapPoints(updateSession.getKnowledgeGapPoints());

        userDigitalProfileService.refreshByUserId(visitorSession.getUserId());
    }

    /**
     * 只要状态和摘要都存在，就默认视为日报已生成。
     */
    private boolean isSessionAlreadyAnalyzed(VisitorSession visitorSession) {
        return SESSION_STATUS_ANALYZED.equalsIgnoreCase(visitorSession.getSessionStatus())
                && StringUtils.hasText(visitorSession.getSummary());
    }

    /**
     * 从 visitor_session 已落库字段中反组装日报对象，保证接口幂等。
     */
    private AgentSessionAnalysisVO buildAnalysisVOFromSession(VisitorSession visitorSession) {
        return AgentSessionAnalysisVO.builder()
                .summary(visitorSession.getSummary())
                .overallSentiment(visitorSession.getOverallSentiment())
                .sentimentScore(visitorSession.getSentimentScore())
                .focusTopics(parseJsonArrayField(visitorSession.getFocusTopics()))
                .interestTags(parseJsonArrayField(visitorSession.getInterestTags()))
                .serviceSuggestions(parseJsonArrayField(visitorSession.getServiceSuggestions()))
                .knowledgeGapPoints(parseJsonArrayField(visitorSession.getKnowledgeGapPoints()))
                .build();
    }

    /**
     * 当前项目还没有统一鉴权，因此这里对超级管理员账号做显式校验。
     */
    private void validateSuperAdminOperator(SessionAnalysisTriggerDTO triggerDTO) {
        if (triggerDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_EMPTY);
        }
        AuthUtil.requireSuperAdmin();
    }

    /**
     * 校验聊天请求参数。
     */
    private void validateChatAskDTO(ChatAskDTO chatAskDTO) {
        if (chatAskDTO == null || chatAskDTO.getUserId() == null) {
            throw new BaseException(MessageConstant.USER_ID_REQUIRED);
        }
        if (!StringUtils.hasText(chatAskDTO.getContent()) && !StringUtils.hasText(chatAskDTO.getVoiceText())) {
            throw new BaseException(MessageConstant.ASK_CONTENT_REQUIRED);
        }
    }

    /**
     * 查询普通用户信息。
     */
    private SysNormalUser getNormalUser(Long userId) {
        SysNormalUser normalUser = sysNormalUserMapper.getById(userId);
        if (normalUser == null || (normalUser.getDeleted() != null && normalUser.getDeleted() == 1)) {
            throw new BaseException(MessageConstant.USER_NOT_FOUND);
        }
        return normalUser;
    }

    /**
     * 获取当天会话；若不存在则创建新会话。
     */
    private VisitorSession getOrCreateSession(ChatAskDTO chatAskDTO, SysNormalUser normalUser, LocalDate reportDate) {
        VisitorSession visitorSession = visitorSessionMapper.getByUserIdAndReportDate(chatAskDTO.getUserId(), reportDate);
        if (visitorSession != null) {
            return visitorSession;
        }

        LocalDateTime now = LocalDateTime.now();
        Integer scenicAreaConfirmed = chatAskDTO.getScenicAreaConfirmed() != null ? chatAskDTO.getScenicAreaConfirmed() : 0;
        String sessionType = chatAskDTO.getScenicAreaId() != null ? SESSION_TYPE_SCENIC_SERVICE : SESSION_TYPE_CONSULTATION;

        /*
         * sessionCode 是发给 Agent 的业务会话编码。
         * visitor_session.id 才是本地数据库主键。
         */
        VisitorSession newSession = VisitorSession.builder()
                .sessionCode(buildSessionCode(chatAskDTO.getUserId(), reportDate))
                .userId(chatAskDTO.getUserId())
                .userNickname(normalUser.getNickname())
                .age(normalUser.getAge())
                .gender(normalUser.getGender() == null ? null : String.valueOf(normalUser.getGender()))
                .reportDate(reportDate)
                .sessionType(sessionType)
                .sessionStatus(SESSION_STATUS_ACTIVE)
                .scenicAreaId(chatAskDTO.getScenicAreaId())
                .scenicAreaSource(chatAskDTO.getScenicAreaId() != null
                        ? defaultIfBlank(chatAskDTO.getScenicAreaSource(), SCENIC_AREA_SOURCE_FRONTEND)
                        : SCENIC_AREA_SOURCE_UNSET)
                .scenicAreaConfirmed(chatAskDTO.getScenicAreaId() != null ? scenicAreaConfirmed : 0)
                .sourceType(chatAskDTO.getSourceType())
                .sourceId(chatAskDTO.getSourceId())
                .interactionCount(0)
                .createTime(now)
                .updateTime(now)
                .build();

        visitorSessionMapper.insert(newSession);
        return newSession;
    }

    /**
     * 如果请求补充了景区信息，则同步刷新会话中的景区字段。
     */
    private VisitorSession refreshScenicAreaInfoIfNecessary(VisitorSession visitorSession, ChatAskDTO chatAskDTO) {
        if (chatAskDTO.getScenicAreaId() == null) {
            return visitorSession;
        }

        boolean needUpdate = visitorSession.getScenicAreaId() == null
                || !chatAskDTO.getScenicAreaId().equals(visitorSession.getScenicAreaId())
                || (chatAskDTO.getScenicAreaConfirmed() != null
                && !chatAskDTO.getScenicAreaConfirmed().equals(visitorSession.getScenicAreaConfirmed()));
        if (!needUpdate) {
            return visitorSession;
        }

        VisitorSession updateSession = VisitorSession.builder()
                .id(visitorSession.getId())
                .scenicAreaId(chatAskDTO.getScenicAreaId())
                .scenicAreaSource(defaultIfBlank(chatAskDTO.getScenicAreaSource(), SCENIC_AREA_SOURCE_FRONTEND))
                .scenicAreaConfirmed(chatAskDTO.getScenicAreaConfirmed() != null ? chatAskDTO.getScenicAreaConfirmed() : 0)
                .sessionType(SESSION_TYPE_SCENIC_SERVICE)
                .build();
        visitorSessionMapper.updateScenicAreaInfo(updateSession);

        visitorSession.setScenicAreaId(updateSession.getScenicAreaId());
        visitorSession.setScenicAreaSource(updateSession.getScenicAreaSource());
        visitorSession.setScenicAreaConfirmed(updateSession.getScenicAreaConfirmed());
        visitorSession.setSessionType(updateSession.getSessionType());
        return visitorSession;
    }

    /**
     * 优先使用语音转写文本，其次使用普通文本内容。
     */
    private String resolveAskContent(ChatAskDTO chatAskDTO) {
        if (StringUtils.hasText(chatAskDTO.getVoiceText())) {
            return chatAskDTO.getVoiceText().trim();
        }
        return chatAskDTO.getContent().trim();
    }

    /**
     * 统一补齐消息类型默认值。
     */
    private String defaultMessageType(String messageType) {
        return StringUtils.hasText(messageType) ? messageType : "text";
    }

    /**
     * 保存一条会话消息。
     * visitor_message.session_id 存的是 visitor_session.id。
     */
    private void saveMessage(VisitorSession visitorSession, Long userId, String senderType, String messageType,
                             String content, String voiceText) {
        Integer maxMessageNo = visitorMessageMapper.getMaxMessageNoBySessionId(visitorSession.getId());
        VisitorMessage visitorMessage = VisitorMessage.builder()
                .sessionId(visitorSession.getId())
                .userId(userId)
                .messageNo(maxMessageNo == null ? 1 : maxMessageNo + 1)
                .senderType(senderType)
                .messageType(defaultMessageType(messageType))
                .content(content)
                .voiceText(voiceText)
                .createTime(LocalDateTime.now())
                .build();
        visitorMessageMapper.insert(visitorMessage);
    }

    /**
     * 统计用户侧发言次数，用于日报分析落库。
     */
    private int countUserInteraction(List<VisitorMessage> messageList) {
        int count = 0;
        for (VisitorMessage visitorMessage : messageList) {
            if ("visitor".equalsIgnoreCase(visitorMessage.getSenderType())) {
                count++;
            }
        }
        return count;
    }

    /**
     * 决定返回给前端的会话类型。
     */
    private String resolveResponseSessionType(VisitorSession visitorSession, AgentChatResponseVO agentResponse) {
        if (StringUtils.hasText(visitorSession.getSessionType())) {
            return visitorSession.getSessionType();
        }
        return defaultIfBlank(agentResponse.getSessionTypeSuggestion(), SESSION_TYPE_CONSULTATION);
    }

    /**
     * 生成业务会话编码。
     */
    private String buildSessionCode(Long userId, LocalDate reportDate) {
        return "session_" + reportDate.format(SESSION_CODE_DATE_FORMATTER) + "_" + userId;
    }

    /**
     * 统一解析日报日期，为空时默认当天。
     */
    private LocalDate resolveReportDate(LocalDate reportDate) {
        return reportDate != null ? reportDate : LocalDate.now();
    }

    /**
     * 将落库的 JSON 数组字段解析回字符串列表。
     */
    private List<String> parseJsonArrayField(String jsonArrayText) {
        if (!StringUtils.hasText(jsonArrayText)) {
            return Collections.emptyList();
        }
        try {
            List<String> result = JSON.parseObject(jsonArrayText, new TypeReference<List<String>>() {
            });
            return result != null ? result : Collections.emptyList();
        } catch (Exception ex) {
            log.warn("解析日报数组字段失败，json={}", jsonArrayText, ex);
            return Collections.emptyList();
        }
    }

    /**
     * 字符串非空则返回原值，否则返回默认值。
     */
    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
