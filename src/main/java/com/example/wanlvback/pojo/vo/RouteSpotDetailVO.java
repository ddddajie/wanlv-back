package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 路线详情中的景点项
 */
@Data
@Builder
public class RouteSpotDetailVO implements Serializable {

    private Long relationId; // 路线景点关联ID

    private Long routeId; // 路线ID

    private Long spotId; // 景点ID

    private Integer sortNo; // 排序号

    private Integer stayDurationMinutes; // 停留分钟数

    private Integer isMustVisit; // 是否必游点

    private String remark; // 备注

    private String spotName; // 景点名称

    private String poiType; // 点位类型

    private String iconType; // 图标类型

    private String spotCode; // 景点编码

    private String shortIntro; // 简介

    private String description; // 详细描述

    private String coverImageUrl; // 封面图

    private String audioUrl; // 音频地址

    private String videoUrl; // 视频地址

    private Long knowledgeDocId; // 知识库文档ID

    private Integer recommendedLevel; // 推荐等级

    private BigDecimal longitude; // 经度

    private BigDecimal latitude; // 纬度
}
