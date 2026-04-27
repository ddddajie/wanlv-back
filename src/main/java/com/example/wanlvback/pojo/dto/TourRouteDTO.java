package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 路线数据传输对象
 */
@Data
public class TourRouteDTO implements Serializable {

    private Long id; // 主键ID

    private Long scenicAreaId; // 所属景区ID

    private String routeName; // 路线名称

    private String routeType; // 路线类型

    private String suitableCrowd; // 适合人群说明

    private Integer durationMinutes; // 预计游览时长

    private Integer distanceMeters; // 路线总距离

    private String description; // 路线说明

    private String recommendedReason; // 推荐理由

    private Integer status; // 状态

    private List<TourRouteSpotDTO> routeSpots; // 路线关联景点
}
