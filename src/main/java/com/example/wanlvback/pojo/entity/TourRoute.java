package com.example.wanlvback.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 游览路线实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourRoute implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id; // 主键 ID

    private Long scenicAreaId; // 景区 ID

    private String routeName; // 路线名称

    private String routeType; // 路线类型

    private String suitableCrowd; // 适合人群

    private Integer durationMinutes; // 游览时长

    private Integer distanceMeters; // 距离

    private String description; // 描述

    private String recommendedReason; // 推荐理由

    private Integer status; // 状态

    private Integer deleted; // 删除标记

    private Long createBy; // 创建人 ID

    private LocalDateTime createTime; // 创建时间

    private Long updateBy; // 更新人 ID

    private LocalDateTime updateTime; // 更新时间
}
