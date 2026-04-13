package com.example.wanlvback.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 游客会话实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 数据库主键 ID。
     */
    private Long id;

    /**
     * 业务会话编码。
     * 该字段用于后端与 Agent 服务保持会话连续性，不等同于数据库主键。
     */
    private String sessionCode;

    private Long userId;
    private String userNickname;
    private Integer age;
    private String gender;
    private LocalDate reportDate;
    private String sessionType;
    private String sessionStatus;
    private Long scenicAreaId;
    private String scenicAreaSource;
    private Integer scenicAreaConfirmed;
    private String sourceType;
    private String sourceId;
    private String attractionName;
    private String attractionType;
    private String attractionContent;
    private BigDecimal stayDuration;
    private Integer groupSize;
    private BigDecimal ticketCost;
    private BigDecimal foodCost;
    private BigDecimal shoppingCost;
    private BigDecimal transportCost;
    private BigDecimal entertainmentCost;
    private BigDecimal totalCost;
    private Integer satisfaction;
    private Integer interactionCount;
    private String summary;
    private String overallSentiment;
    private BigDecimal sentimentScore;
    private String focusTopics;
    private String interestTags;
    private String serviceSuggestions;
    private String knowledgeGapPoints;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
