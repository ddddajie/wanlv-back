package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 地图初始化返回对象
 */
@Data
@Builder
public class MapInitVO implements Serializable {

    private ScenicAreaVO scenicArea; // 景区信息及地图配置

    private List<ScenicGeoFeatureVO> geoFeatures; // 景区空间要素

    private List<ScenicSpotVO> spots; // 景点点位

    private List<MapRouteVO> routes; // 路线信息
}
