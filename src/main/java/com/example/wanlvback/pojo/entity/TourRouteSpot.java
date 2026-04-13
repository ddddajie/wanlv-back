package com.example.wanlvback.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 路线景点关联实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourRouteSpot implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id; // 主键 ID

    private Long routeId; // 路线 ID

    private Long spotId; // 景点 ID

    private Integer sortNo; // 排序号

    private Integer stayDurationMinutes; // 停留分钟数

    private String remark; // 备注

    private LocalDateTime createTime; // 创建时间
}
