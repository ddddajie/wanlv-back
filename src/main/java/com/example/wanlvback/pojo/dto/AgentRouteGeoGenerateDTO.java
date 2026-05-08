package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Agent 定制路线轨迹生成请求参数
 */
@Data
public class AgentRouteGeoGenerateDTO implements Serializable {

    private Long userId; // 所属用户ID

    private String scenicName; // 景区名称

    private String routeName; // 路线名称，未传时使用默认名称

    private List<String> spotNames; // Agent 已排好序的景点名称列表

    private List<String> roadTypes; // 道路子类型过滤

    private Double snapToleranceMeters; // 景点吸附容忍距离

    private String fallbackStrategy; // 兜底策略：DIRECT_SEGMENT/FAIL
}
