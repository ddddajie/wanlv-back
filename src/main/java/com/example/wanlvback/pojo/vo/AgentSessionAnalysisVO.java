package com.example.wanlvback.pojo.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Agent 会话分析返回结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentSessionAnalysisVO implements Serializable {

    private String summary; // 当天会话总结

    @JSONField(name = "overall_sentiment")
    @JsonProperty("overall_sentiment")
    private String overallSentiment; // 整体情感倾向

    @JSONField(name = "sentiment_score")
    @JsonProperty("sentiment_score")
    private BigDecimal sentimentScore; // 情感分值

    @JSONField(name = "focus_topics")
    @JsonProperty("focus_topics")
    private List<String> focusTopics; // 关注话题列表

    @JSONField(name = "interest_tags")
    @JsonProperty("interest_tags")
    private List<String> interestTags; // 兴趣标签列表

    @JSONField(name = "service_suggestions")
    @JsonProperty("service_suggestions")
    private List<String> serviceSuggestions; // 服务建议列表

    @JSONField(name = "knowledge_gap_points")
    @JsonProperty("knowledge_gap_points")
    private List<String> knowledgeGapPoints; // 知识缺口列表
}
