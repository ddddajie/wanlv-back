package com.example.wanlvback.service.impl;

import com.alibaba.fastjson.JSON;
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
import com.example.wanlvback.service.ChatService;
import com.example.wanlvback.utils.AgentChatHttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 聊天业务实现类。
 * 负责本地会话编排、消息落库和结果组装。
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {
    // 普通咨询会话类型。
    private static final String SESSION_TYPE_CONSULTATION = "CONSULTATION";
    // 景区服务会话类型。
    private static final String SESSION_TYPE_SCENIC_SERVICE = "SCENIC_SERVICE";

    // 会话进行中状态。
    private static final String SESSION_STATUS_ACTIVE = "ACTIVE";

    // 会话分析完成状态。
    private static final String SESSION_STATUS_ANALYZED = "ANALYZED";

    // 未设置景区来源。
    private static final String SCENIC_AREA_SOURCE_UNSET = "UNSET";

    // 景区来源为前端传入。
    private static final String SCENIC_AREA_SOURCE_FRONTEND = "FRONTEND";

    // 会话编码日期格式。
    private static final DateTimeFormatter SESSION_CODE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    // 注入会话 Mapper。
    @Autowired
    private VisitorSessionMapper visitorSessionMapper;

    // 注入消息 Mapper。
    @Autowired
    private VisitorMessageMapper visitorMessageMapper;

    // 注入普通用户 Mapper。
    @Autowired
    private SysNormalUserMapper sysNormalUserMapper;

    // 注入 Agent HTTP 工具类。
    @Autowired
    private AgentChatHttpUtil agentChatHttpUtil;

    /**
     * 处理聊天请求。
     * 核心流程：校验参数 -> 获取/创建会话 -> 保存用户消息 -> 调用 Agent -> 保存回复消息。
     *
     * @param chatAskDTO 前端聊天参数
     * @return 聊天回复结果
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

        // 先保存用户提问消息，保证本地会话链路完整。
        saveMessage(
                visitorSession,
                chatAskDTO.getUserId(),
                "visitor",
                defaultMessageType(chatAskDTO.getMessageType()),
                StringUtils.hasText(chatAskDTO.getContent()) ? chatAskDTO.getContent() : askContent,
                chatAskDTO.getVoiceText()
        );

        // Agent 请求、超时处理和响应解析统一由工具类负责。
        AgentChatResponseVO agentResponse = agentChatHttpUtil.requestChat(visitorSession, normalUser, chatAskDTO, askContent);

        // 保存 Agent 回复，确保 visitor_message 表中的一问一答都可追溯。
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
     * 处理会话分析请求。
     *
     * @param triggerDTO 会话分析参数
     * @return 分析结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentSessionAnalysisVO analyzeSession(SessionAnalysisTriggerDTO triggerDTO) {
        if (triggerDTO == null || triggerDTO.getUserId() == null) {
            throw new BaseException("用户ID不能为空");
        }

        LocalDate reportDate = triggerDTO.getReportDate() != null ? triggerDTO.getReportDate() : LocalDate.now();
        VisitorSession visitorSession = visitorSessionMapper.getByUserIdAndReportDate(triggerDTO.getUserId(), reportDate);
        if (visitorSession == null) {
            throw new BaseException("当前日期暂无可分析的会话");
        }

        List<VisitorMessage> messageList = visitorMessageMapper.listBySessionId(visitorSession.getId());
        if (messageList == null || messageList.isEmpty()) {
            throw new BaseException("当前会话暂无消息记录");
        }

        AgentSessionAnalysisResponseVO responseVO = agentChatHttpUtil.requestSessionAnalysis(visitorSession, messageList);
        AgentSessionAnalysisVO analysisVO = responseVO.getAnalysis();
        if (analysisVO == null) {
            throw new BaseException("Agent 未返回有效分析结果");
        }

        // 将分析结果回写到 visitor_session 表，便于后续报表和排查使用。
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
        return analysisVO;
    }

    /**
     * 绑定当前用户当天会话的景区信息。
     *
     * @param bindDTO 景区绑定参数
     * @return 会话主键 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long bindScenicArea(SessionScenicAreaBindDTO bindDTO) {
        if (bindDTO == null || bindDTO.getUserId() == null) {
            throw new BaseException("用户ID不能为空");
        }
        if (bindDTO.getScenicAreaId() == null) {
            throw new BaseException("景区ID不能为空");
        }

        VisitorSession visitorSession = visitorSessionMapper.getByUserIdAndReportDate(bindDTO.getUserId(), LocalDate.now());
        if (visitorSession == null) {
            throw new BaseException("当前日期暂无可绑定景区的会话");
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
     * 校验聊天请求参数。
     *
     * @param chatAskDTO 聊天请求参数
     */
    private void validateChatAskDTO(ChatAskDTO chatAskDTO) {
        if (chatAskDTO == null || chatAskDTO.getUserId() == null) {
            throw new BaseException("用户ID不能为空");
        }
        if (!StringUtils.hasText(chatAskDTO.getContent()) && !StringUtils.hasText(chatAskDTO.getVoiceText())) {
            throw new BaseException("提问内容不能为空");
        }
    }

    /**
     * 查询普通用户信息。
     *
     * @param userId 用户 ID
     * @return 普通用户实体
     */
    private SysNormalUser getNormalUser(Long userId) {
        SysNormalUser normalUser = sysNormalUserMapper.getById(userId);
        if (normalUser == null || (normalUser.getDeleted() != null && normalUser.getDeleted() == 1)) {
            throw new BaseException("用户不存在");
        }
        return normalUser;
    }

    /**
     * 获取当天会话；若不存在则创建新会话。
     *
     * @param chatAskDTO 聊天参数
     * @param normalUser 当前用户
     * @param reportDate 业务日期
     * @return 会话实体
     */
    private VisitorSession getOrCreateSession(ChatAskDTO chatAskDTO, SysNormalUser normalUser, LocalDate reportDate) {
        VisitorSession visitorSession = visitorSessionMapper.getByUserIdAndReportDate(chatAskDTO.getUserId(), reportDate);
        if (visitorSession != null) {
            return visitorSession;
        }

        LocalDateTime now = LocalDateTime.now();
        Integer scenicAreaConfirmed = chatAskDTO.getScenicAreaConfirmed() != null ? chatAskDTO.getScenicAreaConfirmed() : 0;
        String sessionType = chatAskDTO.getScenicAreaId() != null ? SESSION_TYPE_SCENIC_SERVICE : SESSION_TYPE_CONSULTATION;

        // sessionCode 是发给 Agent 的业务会话编码，不是数据库主键。
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

        // 插入后，visitor_session.id 会由数据库自增生成并回填到 newSession.id。
        visitorSessionMapper.insert(newSession);
        return newSession;
    }

    /**
     * 如果本次请求带了景区信息，则同步刷新会话中的景区字段。
     *
     * @param visitorSession 当前会话
     * @param chatAskDTO 聊天参数
     * @return 刷新后的会话对象
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

        // 同步更新内存对象，避免后续继续使用旧值。
        visitorSession.setScenicAreaId(updateSession.getScenicAreaId());
        visitorSession.setScenicAreaSource(updateSession.getScenicAreaSource());
        visitorSession.setScenicAreaConfirmed(updateSession.getScenicAreaConfirmed());
        visitorSession.setSessionType(updateSession.getSessionType());
        return visitorSession;
    }

    /**
     * 统一提取本次提问内容。
     * 优先使用语音转写文本，其次使用普通文本内容。
     *
     * @param chatAskDTO 聊天参数
     * @return 实际提问内容
     */
    private String resolveAskContent(ChatAskDTO chatAskDTO) {
        if (StringUtils.hasText(chatAskDTO.getVoiceText())) {
            return chatAskDTO.getVoiceText().trim();
        }
        return chatAskDTO.getContent().trim();
    }

    /**
     * 统一补齐消息类型默认值。
     *
     * @param messageType 原始消息类型
     * @return 实际消息类型
     */
    private String defaultMessageType(String messageType) {
        return StringUtils.hasText(messageType) ? messageType : "text";
    }

    /**
     * 保存一条会话消息。
     * 注意：visitor_message.session_id 存的是 visitor_session.id，也就是数据库主键。
     *
     * @param visitorSession 当前会话
     * @param userId 用户 ID
     * @param senderType 发送方类型
     * @param messageType 消息类型
     * @param content 文本内容
     * @param voiceText 语音转写文本
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
     * 统计用户侧发言次数，用于分析结果落库。
     *
     * @param messageList 消息列表
     * @return 用户发言次数
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
     *
     * @param visitorSession 当前会话
     * @param agentResponse Agent 响应
     * @return 会话类型
     */
    private String resolveResponseSessionType(VisitorSession visitorSession, AgentChatResponseVO agentResponse) {
        if (StringUtils.hasText(visitorSession.getSessionType())) {
            return visitorSession.getSessionType();
        }
        return defaultIfBlank(agentResponse.getSessionTypeSuggestion(), SESSION_TYPE_CONSULTATION);
    }

    /**
     * 生成业务会话编码，格式为 session_yyyyMMdd_userId。
     *
     * @param userId 用户 ID
     * @param reportDate 业务日期
     * @return 业务会话编码
     */
    private String buildSessionCode(Long userId, LocalDate reportDate) {
        return "session_" + reportDate.format(SESSION_CODE_DATE_FORMATTER) + "_" + userId;
    }

    /**
     * 字符串非空则返回原值，否则返回默认值。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后的字符串
     */
    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
