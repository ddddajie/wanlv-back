package com.example.wanlvback.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 景点实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenicSpot implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id; // 主键ID

    private Long scenicAreaId; // 景区ID

    private String spotName; // 景点名称

    private String poiType; // 点位类型

    private String iconType; // 地图图标类型

    private String spotCode; // 景点编码

    private String shortIntro; // 简介

    private String description; // 详细描述

    private BigDecimal longitude; // 经度

    private BigDecimal latitude; // 纬度

    private Integer stayDurationMinutes; // 停留分钟数

    private String openingHours; // 开放时间

    private String coverImageUrl; // 封面图片

    private String audioUrl; // 音频地址

    private String videoUrl; // 视频地址

    private Long knowledgeDocId; // 关联知识库文档ID

    private Integer recommendedLevel; // 推荐等级

    private Integer sortNo; // 排序号

    private Integer status; // 状态

    private Integer reservationEnabled; // 是否支持预约：0-不支持，1-支持

    private String reservationNotice; // 预约须知

    private Integer advanceReservationDays; // 最多可提前预约天数

    private Integer minAdvanceMinutes; // 最少提前预约分钟数

    private Integer deleted; // 删除标记

    private Long createBy; // 创建人ID

    private LocalDateTime createTime; // 创建时间

    private Long updateBy; // 更新人ID

    private LocalDateTime updateTime; // 更新时间
}
