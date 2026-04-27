package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 景区地图空间要素数据传输对象
 */
@Data
public class ScenicGeoFeatureDTO implements Serializable {

    private Long id; // 主键ID

    private Long scenicAreaId; // 所属景区ID

    private String featureName; // 区域名称

    private String featureType; // 要素类型

    private String geojson; // 区域GeoJSON数据

    private Integer status; // 状态

    private Integer deleted; // 逻辑删除
}
