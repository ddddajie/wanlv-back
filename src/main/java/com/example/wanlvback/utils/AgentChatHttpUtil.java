package com.example.wanlvback.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.wanlvback.exception.BaseException;
import com.example.wanlvback.mapper.UserDigitalProfileMapper;
import com.example.wanlvback.pojo.dto.ChatAskDTO;
import com.example.wanlvback.pojo.entity.SysNormalUser;
import com.example.wanlvback.pojo.entity.UserDigitalProfile;
import com.example.wanlvback.pojo.entity.VisitorMessage;
import com.example.wanlvback.pojo.entity.VisitorSession;
import com.example.wanlvback.pojo.vo.AgentChatResponseVO;
import com.example.wanlvback.pojo.vo.AgentSessionAnalysisResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Agent 聊天 HTTP 工具类。
 * 统一负责请求组装、HTTP 调用、超时处理和响应解析。
 */
@Component
@Slf4j
public class AgentChatHttpUtil {

    @Autowired
    private UserDigitalProfileMapper userDigitalProfileMapper;

    /**
     * Agent 服务基础地址。
     */
    @Value("${wanlv.agent.base-url:http://127.0.0.1:8000}")
    private String agentBaseUrl;

    /**
     * HTTP 建连超时时间，单位毫秒。
     */
    @Value("${wanlv.agent.connect-timeout-ms:5000}")
    private int agentConnectTimeoutMs;

    /**
     * HTTP 读取超时时间，单位毫秒。
     */
    @Value("${wanlv.agent.read-timeout-ms:60000}")
    private int agentReadTimeoutMs;

    /**
     * 调用 Agent 聊天接口。
     *
     * @param visitorSession 当前会话
     * @param normalUser 当前用户
     * @param chatAskDTO 前端提问参数
     * @param askContent 实际提问内容
     * @return Agent 聊天响应
     */
    public AgentChatResponseVO requestChat(VisitorSession visitorSession, SysNormalUser normalUser,
                                           ChatAskDTO chatAskDTO, String askContent) {
        JSONObject requestJson = new JSONObject();
        requestJson.put("query", askContent);
        requestJson.put("session_id", visitorSession.getSessionCode());
        requestJson.put("user_id", visitorSession.getUserId());
        requestJson.put("report_date", visitorSession.getReportDate().toString());
        requestJson.put("scenic_area_id", visitorSession.getScenicAreaId());
        requestJson.put("user_nickname", normalUser.getNickname());
        requestJson.put("age", normalUser.getAge());
        requestJson.put("user_name", normalUser.getUsername());
        requestJson.put("gender", normalUser.getGender() == null ? null : String.valueOf(normalUser.getGender()));
        requestJson.put("message_type", defaultMessageType(chatAskDTO.getMessageType()));
        requestJson.put("voice_text", chatAskDTO.getVoiceText());
        JSONObject userProfile = buildUserProfile(visitorSession.getUserId());
        if (userProfile != null) {
            requestJson.put("user_profile", userProfile);
        }
        log.info("Agent 问答请求用户画像状态，userId={}, hasProfile={}",
                visitorSession.getUserId(), userProfile != null && !userProfile.isEmpty());

        String responseBody = postJson("/chat", requestJson.toJSONString(), visitorSession.getSessionCode(), "问答");
        AgentChatResponseVO responseVO = JSON.parseObject(responseBody, AgentChatResponseVO.class);
        if (responseVO == null) {
            throw new BaseException("Agent 聊天响应解析失败");
        }
        if (responseVO.getCode() == null || responseVO.getCode() != 200 || !StringUtils.hasText(responseVO.getResponse())) {
            throw new BaseException(defaultIfBlank(responseVO.getMessage(), "Agent 聊天处理失败"));
        }
        return responseVO;
    }

    private JSONObject buildUserProfile(Long userId) {
        if (userId == null) {
            return null;
        }

        UserDigitalProfile profile = userDigitalProfileMapper.getByUserId(userId);
        if (profile == null) {
            return null;
        }

        if (StringUtils.hasText(profile.getProfileJson())) {
            try {
                JSONObject profileJson = JSON.parseObject(profile.getProfileJson());
                if (profileJson != null && !profileJson.isEmpty()) {
                    return profileJson;
                }
            } catch (RuntimeException ex) {
                log.warn("解析用户数字画像 JSON 失败，userId={}", userId, ex);
            }
        }

        JSONObject profileJson = new JSONObject(true);
        profileJson.put("profileName", profile.getProfileName());
        profileJson.put("interestTags", parseJsonArrayField(profile.getInterestTags()));
        profileJson.put("focusTopics", parseJsonArrayField(profile.getFocusTopics()));
        profileJson.put("serviceNeeds", parseJsonArrayField(profile.getServiceNeeds()));
        profileJson.put("knowledgeGaps", parseJsonArrayField(profile.getKnowledgeGaps()));
        profileJson.put("travelStyle", profile.getTravelStyle());
        profileJson.put("activityLevel", profile.getActivityLevel());
        profileJson.put("sentimentTendency", profile.getSentimentTendency());
        profileJson.put("sentimentScoreAvg", profile.getSentimentScoreAvg());
        profileJson.put("profileScore", profile.getProfileScore());
        profileJson.put("sourceSessionCount", profile.getSourceSessionCount());
        profileJson.put("lastAnalyzedDate", profile.getLastAnalyzedDate());
        return profileJson;
    }

    private List<String> parseJsonArrayField(String jsonArrayText) {
        if (!StringUtils.hasText(jsonArrayText)) {
            return List.of();
        }
        try {
            List<String> values = JSON.parseArray(jsonArrayText, String.class);
            return values == null ? List.of() : values;
        } catch (RuntimeException ex) {
            log.warn("解析用户数字画像数组字段失败，json={}", jsonArrayText, ex);
            return List.of();
        }
    }

    /**
     * 调用 Agent 会话分析接口。
     *
     * @param visitorSession 当前会话
     * @param messageList 当前会话消息列表
     * @return 会话分析结果
     */
    public AgentSessionAnalysisResponseVO requestSessionAnalysis(VisitorSession visitorSession,
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

        String responseBody = postJson("/analyze-session", requestJson.toJSONString(), visitorSession.getSessionCode(), "会话分析");
        AgentSessionAnalysisResponseVO responseVO = JSON.parseObject(responseBody, AgentSessionAnalysisResponseVO.class);
        if (responseVO == null) {
            throw new BaseException("Agent 会话分析响应解析失败");
        }
        if (responseVO.getCode() == null || responseVO.getCode() != 200 || responseVO.getAnalysis() == null) {
            throw new BaseException(defaultIfBlank(responseVO.getMessage(), "Agent 会话分析失败"));
        }
        return responseVO;
    }

    /**
     * 执行 Agent POST JSON 请求。
     *
     * @param path 接口路径
     * @param jsonBody 请求体 JSON
     * @param sessionCode 业务会话编码
     * @param actionName 当前调用动作名称
     * @return 原始响应体
     */
    private String postJson(String path, String jsonBody, String sessionCode, String actionName) {
        String url = agentBaseUrl + path;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(buildRequestConfig());

            if (jsonBody != null) {
                StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
                entity.setContentEncoding(StandardCharsets.UTF_8.name());
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (!StringUtils.hasText(responseBody)) {
                    throw new BaseException("Agent " + actionName + "服务返回为空");
                }
                return responseBody;
            }
        } catch (SocketTimeoutException e) {
            log.error("调用 Agent {}接口超时，url={}, sessionCode={}, connectTimeoutMs={}, readTimeoutMs={}",
                    actionName, url, sessionCode, agentConnectTimeoutMs, agentReadTimeoutMs, e);
            throw new BaseException("Agent " + actionName + "服务响应超时，请稍后重试");
        } catch (IOException e) {
            log.error("调用 Agent {}接口失败，url={}, sessionCode={}", actionName, url, sessionCode, e);
            throw new BaseException("调用 Agent " + actionName + "服务失败");
        }
    }

    /**
     * 构建统一的 HTTP 超时配置。
     *
     * @return HTTP 请求配置
     */
    private RequestConfig buildRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(agentConnectTimeoutMs)
                .setConnectionRequestTimeout(agentConnectTimeoutMs)
                .setSocketTimeout(agentReadTimeoutMs)
                .build();
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
     * 将本地发送方类型转换为 Agent 需要的 role。
     *
     * @param senderType 发送方类型
     * @return Agent 角色值
     */
    private String resolveAnalysisRole(String senderType) {
        if ("agent".equalsIgnoreCase(senderType)) {
            return "assistant";
        }
        return "user";
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
