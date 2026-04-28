package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 景区地图空间要素返回对象
 */
@Data
@Builder
public class ScenicGeoFeatureVO implements Serializable {

    private Long id; // 主键ID

    private Long scenicAreaId; // 所属景区ID

    private String featureName; // 要素名称

    private String featureType; // 要素类型

    private String geometryType; // 几何类型：POINT/LINE/POLYGON

    private String featureSubType; // 要素子类型

    private Integer lengthMeters; // 长度（米）

    private String propertiesJson; // 扩展属性JSON

    private String geojson; // GeoJSON数据

    private Integer status; // 状态

    private Integer deleted; // 逻辑删除

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间
}
