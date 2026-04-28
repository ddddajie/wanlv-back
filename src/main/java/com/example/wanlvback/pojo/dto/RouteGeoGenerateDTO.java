package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 路线轨迹自动生成请求参数
 */
@Data
public class RouteGeoGenerateDTO implements Serializable {

    private List<String> roadTypes; // 道路子类型过滤

    private Double snapToleranceMeters; // 景点吸附容忍距离

    private Boolean saveAsVersion; // 是否保存为轨迹版本

    private Integer version; // 轨迹版本号

    private Integer status; // 保存版本状态

    private Boolean overwriteActive; // 是否停用已有启用版本

    private String fallbackStrategy; // 兜底策略：DIRECT_SEGMENT/FAIL
}
