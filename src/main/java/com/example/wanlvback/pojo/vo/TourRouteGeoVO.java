package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 路线几何数据返回对象
 */
@Data
@Builder
public class TourRouteGeoVO implements Serializable {

    private Long id; // 主键ID

    private Long routeId; // 路线ID

    private Long scenicAreaId; // 所属景区ID

    private String geojson; // 路线GeoJSON数据

    private Integer version; // 版本号

    private Integer status; // 状态

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间
}
