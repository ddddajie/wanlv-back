package com.example.wanlvback.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Agent 生成的用户定制路线几何数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentRouteGeo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id; // 主键ID

    private Long userId; // 所属用户ID

    private Long scenicAreaId; // 所属景区ID

    private String routeName; // 路线名称

    private String geojson; // 路线GeoJSON数据

    private String spotIdsJson; // 景点ID顺序JSON

    private String spotNamesJson; // 景点名称顺序JSON

    private Double distanceMeters; // 路线距离，单位米

    private Integer spotCount; // 景点数量

    private Integer roadSegmentCount; // 道路片段数量

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间
}
