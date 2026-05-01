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
 * 用户数字画像实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDigitalProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String profileName;
    private String interestTags;
    private String focusTopics;
    private String serviceNeeds;
    private String knowledgeGaps;
    private String travelStyle;
    private String activityLevel;
    private String sentimentTendency;
    private BigDecimal sentimentScoreAvg;
    private Integer profileScore;
    private Integer sourceSessionCount;
    private LocalDate lastAnalyzedDate;
    private String profileJson;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
