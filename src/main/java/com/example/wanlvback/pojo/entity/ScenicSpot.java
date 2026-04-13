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

    private Long id; // 主键 ID

    private Long scenicAreaId; // 景区 ID

    private String spotName; // 景点名称

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

    private Integer sortNo; // 排序号

    private Integer status; // 状态

    private Integer deleted; // 删除标记

    private Long createBy; // 创建人 ID

    private LocalDateTime createTime; // 创建时间

    private Long updateBy; // 更新人 ID

    private LocalDateTime updateTime; // 更新时间
}
