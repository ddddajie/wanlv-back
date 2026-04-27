package com.example.wanlvback.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 景区地图空间要素实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenicGeoFeature implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id; // 主键ID

    private Long scenicAreaId; // 所属景区ID

    private String featureName; // 区域名称

    private String featureType; // 要素类型

    private String geojson; // 区域GeoJSON数据

    private Integer status; // 状态

    private Integer deleted; // 逻辑删除

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间
}
