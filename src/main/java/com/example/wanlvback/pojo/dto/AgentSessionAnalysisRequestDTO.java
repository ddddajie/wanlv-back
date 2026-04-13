package com.example.wanlvback.pojo.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 后端日终调用 Agent 会话分析请求参数
 */
@Data
public class AgentSessionAnalysisRequestDTO implements Serializable {

    @JSONField(name = "user_id")
    @JsonProperty("user_id")
    private Long userId; // 用户 ID

    @JSONField(name = "report_date")
    @JsonProperty("report_date")
    private LocalDate reportDate; // 日报日期

    @JSONField(name = "session_id")
    @JsonProperty("session_id")
    private String sessionId; // 当天唯一会话编码

    @JSONField(name = "scenic_area_id")
    @JsonProperty("scenic_area_id")
    private Long scenicAreaId; // 景区 ID，可为空

    @JSONField(name = "session_type")
    @JsonProperty("session_type")
    private String sessionType; // 会话类型

    private List<AnalysisMessageDTO> messages; // 会话消息列表
}
