package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 前端聊天响应结果
 */
@Data
@Builder
public class ChatAnswerVO implements Serializable {

    private String answer; // Agent 回复内容

    private Long sessionId; // 本地会话主键 ID

    private String sessionCode; // 用于 Agent 历史关联的会话编码

    private LocalDate reportDate; // 当天会话日期

    private String sessionType; // 会话类型

    private Long scenicAreaId; // 当前绑定的景区 ID

    private Integer scenicAreaConfirmed; // 本次响应后景区是否已确认

    private Long detectedScenicAreaId; // Agent 识别出的候选景区 ID

    private String detectedScenicAreaName; // Agent 识别出的候选景区名称

    private Double detectionConfidence; // Agent 候选景区识别置信度

    private Boolean needScenicAreaConfirm; // 是否需要前端引导用户继续确认景区
}
