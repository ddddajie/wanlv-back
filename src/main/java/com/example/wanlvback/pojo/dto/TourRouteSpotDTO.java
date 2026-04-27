package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 路线景点关联数据传输对象
 */
@Data
public class TourRouteSpotDTO implements Serializable {

    private Long id; // 主键ID

    private Long routeId; // 路线ID

    private Long spotId; // 景点ID

    private Integer sortNo; // 排序号

    private Integer stayDurationMinutes; // 停留分钟数

    private Integer isMustVisit; // 是否必游点

    private String remark; // 备注
}
