package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 路线详情返回对象
 */
@Data
@Builder
public class RouteDetailVO implements Serializable {

    private TourRouteVO route; // 路线基础信息

    private TourRouteGeoVO routeGeo; // 最新启用几何数据

    private List<RouteSpotDetailVO> spots; // 路线景点列表
}
