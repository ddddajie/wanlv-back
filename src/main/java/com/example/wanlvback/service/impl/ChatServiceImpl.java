package com.example.wanlvback.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import com.example.wanlvback.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 聊天业务实现类
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
    private static final DateTimeFormatter SESSION_CODE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final VisitorSessionMapper visitorSessionMapper;
    private final VisitorMessageMapper visitorMessageMapper;
    private final SysNormalUserMapper sysNormalUserMapper;

    @Value("${wanlv.agent.base-url:http://127.0.0.1:8000}")
    private String agentBaseUrl;

    @Value("${wanlv.agent.connect-timeout-ms:5000}")
    private int agentConnectTimeoutMs;

    @Value("${wanlv.agent.read-timeout-ms:60000}")
    private int agentReadTimeoutMs;

    public ChatServiceImpl(VisitorSessionMapper visitorSessionMapper,
                           VisitorMessageMapper visitorMessageMapper,
                           SysNormalUserMapper sysNormalUserMapper) {
        this.visitorSessionMapper = visitorSessionMapper;
        this.visitorMessageMapper = visitorMessageMapper;
        this.sysNormalUserMapper = sysNormalUserMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatAnswerVO ask(ChatAskDTO chatAskDTO) {
        validateChatAskDTO(chatAskDTO);
        LocalDate reportDate = LocalDate.now();
        SysNormalUser normalUser = getNormalUser(chatAskDTO.getUserId());
        VisitorSession visitorSession = getOrCreateSession(chatAskDTO, normalUser, reportDate);
        visitorSession = refreshScenicAreaInfoIfNecessary(visitorSession, chatAskDTO);

        String askContent = resolveAskContent(chatAskDTO);
        saveMessage(visitorSession, chatAskDTO.getUserId(), "visitor", defaultMessageType(chatAskDTO.getMessageType()),
                StringUtils.hasText(chatAskDTO.getContent()) ? chatAskDTO.getContent() : askContent, chatAskDTO.getVoiceText());

        AgentChatResponseVO agentResponse = invokeAgentChat(visitorSession, normalUser, chatAskDTO, askContent);
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

        AgentSessionAnalysisResponseVO responseVO = invokeAgentSessionAnalysis(visitorSession, messageList);
        AgentSessionAnalysisVO analysisVO = responseVO.getAnalysis();
        if (analysisVO == null) {
            throw new BaseException("Agent 未返回有效分析结果");
        }

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

    private void validateChatAskDTO(ChatAskDTO chatAskDTO) {
        if (chatAskDTO == null || chatAskDTO.getUserId() == null) {
            throw new BaseException("用户ID不能为空");
        }
        if (!StringUtils.hasText(chatAskDTO.getContent()) && !StringUtils.hasText(chatAskDTO.getVoiceText())) {
            throw new BaseException("提问内容不能为空");
        }
    }

    private SysNormalUser getNormalUser(Long userId) {
        SysNormalUser normalUser = sysNormalUserMapper.getById(userId);
        if (normalUser == null || (normalUser.getDeleted() != null && normalUser.getDeleted() == 1)) {
            throw new BaseException("用户不存在");
        }
        return normalUser;
    }

    private VisitorSession getOrCreateSession(ChatAskDTO chatAskDTO, SysNormalUser normalUser, LocalDate reportDate) {
        VisitorSession visitorSession = visitorSessionMapper.getByUserIdAndReportDate(chatAskDTO.getUserId(), reportDate);
        if (visitorSession != null) {
            return visitorSession;
        }

        LocalDateTime now = LocalDateTime.now();
        Integer scenicAreaConfirmed = chatAskDTO.getScenicAreaConfirmed() != null ? chatAskDTO.getScenicAreaConfirmed() : 0;
        String sessionType = chatAskDTO.getScenicAreaId() != null ? SESSION_TYPE_SCENIC_SERVICE : SESSION_TYPE_CONSULTATION;
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

    private String resolveAskContent(ChatAskDTO chatAskDTO) {
        if (StringUtils.hasText(chatAskDTO.getVoiceText())) {
            return chatAskDTO.getVoiceText().trim();
        }
        return chatAskDTO.getContent().trim();
    }

    private String defaultMessageType(String messageType) {
        return StringUtils.hasText(messageType) ? messageType : "text";
    }

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

    private AgentChatResponseVO invokeAgentChat(VisitorSession visitorSession, SysNormalUser normalUser,
                                                ChatAskDTO chatAskDTO, String askContent) {
        JSONObject requestJson = new JSONObject();
        requestJson.put("query", askContent);
        requestJson.put("session_id", visitorSession.getSessionCode());
        requestJson.put("user_id", visitorSession.getUserId());
        requestJson.put("report_date", visitorSession.getReportDate().toString());
        requestJson.put("scenic_area_id", visitorSession.getScenicAreaId());
        requestJson.put("user_nickname", normalUser.getNickname());
        requestJson.put("age", normalUser.getAge());
        requestJson.put("gender", normalUser.getGender() == null ? null : String.valueOf(normalUser.getGender()));
        requestJson.put("message_type", defaultMessageType(chatAskDTO.getMessageType()));
        requestJson.put("voice_text", chatAskDTO.getVoiceText());

        String responseBody;
        try {
            responseBody = HttpClientUtil.doPost4Json(agentBaseUrl + "/chat", requestJson.toJSONString(),
                    agentConnectTimeoutMs, agentReadTimeoutMs);
        } catch (SocketTimeoutException e) {
            log.error("调用Agent问答接口超时, url={}, sessionCode={}, connectTimeoutMs={}, readTimeoutMs={}",
                    agentBaseUrl + "/chat", visitorSession.getSessionCode(), agentConnectTimeoutMs, agentReadTimeoutMs, e);
            throw new BaseException("Agent服务响应超时，请稍后重试");
        } catch (Exception e) {
            log.error("调用Agent问答接口失败, url={}, sessionCode={}",
                    agentBaseUrl + "/chat", visitorSession.getSessionCode(), e);
            throw new BaseException("调用Agent服务失败");
        }

        if (!StringUtils.hasText(responseBody)) {
            throw new BaseException("Agent服务返回为空");
        }

        AgentChatResponseVO responseVO = JSON.parseObject(responseBody, AgentChatResponseVO.class);
        if (responseVO == null) {
            throw new BaseException("Agent服务响应解析失败");
        }
        if (responseVO.getCode() == null || responseVO.getCode() != 200 || !StringUtils.hasText(responseVO.getResponse())) {
            throw new BaseException(defaultIfBlank(responseVO.getMessage(), "Agent服务处理失败"));
        }
        return responseVO;
    }

    private AgentSessionAnalysisResponseVO invokeAgentSessionAnalysis(VisitorSession visitorSession,
                                                                      List<VisitorMessage> messageList) {
        JSONObject requestJson = new JSONObject();
        requestJson.put("user_id", visitorSession.getUserId());
        requestJson.put("report_date", visitorSession.getReportDate().toString());
        requestJson.put("session_id", visitorSession.getSessionCode());
        requestJson.put("scenic_area_id", visitorSession.getScenicAreaId());
        requestJson.put("session_type", visitorSession.getSessionType());

        JSONArray messageArray = new JSONArray();
        for (VisitorMessage visitorMessage : messageList) {
            JSONObject messageJson = new JSONObject();
            messageJson.put("role", resolveAnalysisRole(visitorMessage.getSenderType()));
            messageJson.put("content", visitorMessage.getContent());
            messageArray.add(messageJson);
        }
        requestJson.put("messages", messageArray);

        String responseBody;
        try {
            responseBody = HttpClientUtil.doPost4Json(agentBaseUrl + "/analyze-session", requestJson.toJSONString(),
                    agentConnectTimeoutMs, agentReadTimeoutMs);
        } catch (SocketTimeoutException e) {
            log.error("调用Agent会话分析接口超时, url={}, sessionCode={}, connectTimeoutMs={}, readTimeoutMs={}",
                    agentBaseUrl + "/analyze-session", visitorSession.getSessionCode(), agentConnectTimeoutMs, agentReadTimeoutMs, e);
            throw new BaseException("Agent分析服务响应超时，请稍后重试");
        } catch (Exception e) {
            log.error("调用Agent会话分析接口失败, url={}, sessionCode={}",
                    agentBaseUrl + "/analyze-session", visitorSession.getSessionCode(), e);
            throw new BaseException("调用Agent分析服务失败");
        }

        if (!StringUtils.hasText(responseBody)) {
            throw new BaseException("Agent分析服务返回为空");
        }

        AgentSessionAnalysisResponseVO responseVO = JSON.parseObject(responseBody, AgentSessionAnalysisResponseVO.class);
        if (responseVO == null) {
            throw new BaseException("Agent分析响应解析失败");
        }
        if (responseVO.getCode() == null || responseVO.getCode() != 200 || responseVO.getAnalysis() == null) {
            throw new BaseException(defaultIfBlank(responseVO.getMessage(), "Agent会话分析失败"));
        }
        return responseVO;
    }

    private int countUserInteraction(List<VisitorMessage> messageList) {
        int count = 0;
        for (VisitorMessage visitorMessage : messageList) {
            if ("visitor".equalsIgnoreCase(visitorMessage.getSenderType())) {
                count++;
            }
        }
        return count;
    }

    private String resolveAnalysisRole(String senderType) {
        if ("agent".equalsIgnoreCase(senderType)) {
            return "assistant";
        }
        return "user";
    }

    private String resolveResponseSessionType(VisitorSession visitorSession, AgentChatResponseVO agentResponse) {
        if (StringUtils.hasText(visitorSession.getSessionType())) {
            return visitorSession.getSessionType();
        }
        return defaultIfBlank(agentResponse.getSessionTypeSuggestion(), SESSION_TYPE_CONSULTATION);
    }

    private String buildSessionCode(Long userId, LocalDate reportDate) {
        return "session_" + reportDate.format(SESSION_CODE_DATE_FORMATTER) + "_" + userId;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
