package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 路线几何数据传输对象
 */
@Data
public class TourRouteGeoDTO implements Serializable {

    private Long id; // 主键ID

    private Long routeId; // 路线ID

    private Long scenicAreaId; // 所属景区ID

    private String geojson; // 路线GeoJSON数据

    private Integer version; // 版本号

    private Integer status; // 状态
}
