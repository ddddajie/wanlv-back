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
 * 游客会话实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id; // 主键 ID

    private String sessionCode; // 会话编码

    private String touristId; // 游客编号

    private String userNickname; // 游客昵称

    private Integer age; // 年龄

    private String gender; // 性别

    private Long scenicAreaId; // 景区 ID

    private LocalDate reportDate; // 报告日期

    private String attractionName; // 景点名称

    private String attractionType; // 景点类型

    private String attractionContent; // 景点内容

    private BigDecimal stayDuration; // 停留时长

    private Integer groupSize; // 同行人数

    private BigDecimal ticketCost; // 门票消费

    private BigDecimal foodCost; // 餐饮消费

    private BigDecimal shoppingCost; // 购物消费

    private BigDecimal transportCost; // 交通消费

    private BigDecimal entertainmentCost; // 娱乐消费

    private BigDecimal totalCost; // 总消费

    private Integer satisfaction; // 满意度

    private Integer interactionCount; // 互动次数

    private String summary; // 总结

    private String overallSentiment; // 整体情感

    private BigDecimal sentimentScore; // 情感分值

    private String focusTopics; // 关注话题 JSON

    private String interestTags; // 兴趣标签 JSON

    private String serviceSuggestions; // 服务建议 JSON

    private String knowledgeGapPoints; // 知识缺口 JSON

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间
}
