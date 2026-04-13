package com.example.wanlvback.pojo.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Agent 问答返回结果
 */
@Data
public class AgentChatResponseVO implements Serializable {

    private Integer code; // Agent 返回码

    private String message; // Agent 返回信息

    private String response; // Agent 回答内容

    @JSONField(name = "detected_scenic_area_id")
    @JsonProperty("detected_scenic_area_id")
    private Long detectedScenicAreaId; // 候选景区 ID

    @JSONField(name = "detected_scenic_area_name")
    @JsonProperty("detected_scenic_area_name")
    private String detectedScenicAreaName; // 候选景区名称

    @JSONField(name = "detection_confidence")
    @JsonProperty("detection_confidence")
    private Double detectionConfidence; // 候选景区识别置信度

    @JSONField(name = "need_scenic_area_confirm")
    @JsonProperty("need_scenic_area_confirm")
    private Boolean needScenicAreaConfirm; // 是否需要继续确认景区

    @JSONField(name = "session_type_suggestion")
    @JsonProperty("session_type_suggestion")
    private String sessionTypeSuggestion; // 会话类型建议
}
