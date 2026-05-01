package com.example.wanlvback.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 路线几何数据实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourRouteGeo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id; // 主键ID

    private Long routeId; // 路线ID

    private Long scenicAreaId; // 所属景区ID

    private String geojson; // 路线GeoJSON数据

    private Integer version; // 版本号

    private Integer status; // 状态

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间
}
