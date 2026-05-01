package com.example.wanlvback.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户数字画像返回对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDigitalProfileVO implements Serializable {

    private Long id;
    private Long userId;
    private String profileName;
    private List<String> interestTags;
    private List<String> focusTopics;
    private List<String> serviceNeeds;
    private List<String> knowledgeGaps;
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
