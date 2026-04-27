package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 路线返回对象
 */
@Data
@Builder
public class TourRouteVO implements Serializable {

    private Long id; // 主键ID

    private Long scenicAreaId; // 所属景区ID

    private String routeName; // 路线名称

    private String routeType; // 路线类型

    private String suitableCrowd; // 适合人群说明

    private Integer durationMinutes; // 预计游览时长

    private Integer distanceMeters; // 路线总距离

    private String description; // 路线说明

    private String recommendedReason; // 推荐理由

    private Integer status; // 状态

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间
}
