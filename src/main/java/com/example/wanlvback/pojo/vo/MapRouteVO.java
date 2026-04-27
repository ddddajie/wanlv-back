package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 地图初始化中的路线返回对象
 */
@Data
@Builder
public class MapRouteVO implements Serializable {

    private Long id; // 路线ID

    private Long scenicAreaId; // 景区ID

    private String routeName; // 路线名称

    private String routeType; // 路线类型

    private String suitableCrowd; // 适合人群

    private Integer durationMinutes; // 游览时长

    private Integer distanceMeters; // 路线距离

    private String description; // 路线说明

    private String recommendedReason; // 推荐理由

    private String geojson; // 路线GeoJSON

    private Integer geoVersion; // 几何版本号
}
