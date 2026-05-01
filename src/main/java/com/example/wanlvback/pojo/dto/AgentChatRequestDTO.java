package com.example.wanlvback.pojo.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;

/**
 * 后端转发给 Agent 的问答请求参数
 */
@Data
public class AgentChatRequestDTO implements Serializable {

    private String query; // 用户提问

    @JSONField(name = "session_id")
    @JsonProperty("session_id")
    private String sessionId; // 传递给 Agent 的会话 ID

    @JSONField(name = "user_id")
    @JsonProperty("user_id")
    private Long userId; // 用户 ID

    @JSONField(name = "report_date")
    @JsonProperty("report_date")
    private LocalDate reportDate; // 当天会话日期

    @JSONField(name = "scenic_area_id")
    @JsonProperty("scenic_area_id")
    private Long scenicAreaId; // 当前已绑定的景区 ID，可为空

    @JSONField(name = "user_nickname")
    @JsonProperty("user_nickname")
    private String userNickname; // 用户昵称

    @JSONField(name = "user_name")
    @JsonProperty("user_name")
    private String userName; // 用户账号

    private Integer age; // 用户年龄

    private String gender; // 用户性别

    @JSONField(name = "message_type")
    @JsonProperty("message_type")
    private String messageType; // 消息类型

    @JSONField(name = "voice_text")
    @JsonProperty("voice_text")
    private String voiceText; // 语音转文字文本

    @JSONField(name = "user_profile")
    @JsonProperty("user_profile")
    private Map<String, Object> userProfile; // 用户数字画像，可为空
}
