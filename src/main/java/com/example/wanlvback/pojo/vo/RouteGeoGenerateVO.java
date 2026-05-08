package com.example.wanlvback.pojo.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 路线轨迹自动生成响应
 */
@Data
@Builder
public class RouteGeoGenerateVO implements Serializable {

    private Long routeId; // 路线ID

    private Long scenicAreaId; // 景区ID

    private String routeName; // 路线名称

    private Integer version; // 轨迹版本号

    private Boolean saved; // 是否已保存

    private Long routeGeoId; // 轨迹版本ID

    private Double distanceMeters; // 轨迹长度

    private Integer spotCount; // 景点数量

    private Integer roadSegmentCount; // 使用道路分段数

    private JSONObject geojson; // GeoJSON Feature

    private List<RouteSpotDetailVO> spots; // 本次生成使用的有序景点列表

    private List<RouteGeoGenerateWarningVO> warnings; // 非阻断告警
}
