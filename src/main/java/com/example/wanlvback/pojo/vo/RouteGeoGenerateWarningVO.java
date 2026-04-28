package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 路线轨迹自动生成告警
 */
@Data
@Builder
public class RouteGeoGenerateWarningVO implements Serializable {

    private String code; // 告警编码

    private String message; // 告警信息

    private Long spotId; // 景点ID

    private String spotName; // 景点名称

    private Long featureId; // 空间要素ID

    private String featureName; // 空间要素名称

    private Double distanceMeters; // 距离
}
