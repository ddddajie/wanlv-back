package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 景点数据传输对象
 */
@Data
public class ScenicSpotDTO implements Serializable {

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
}
