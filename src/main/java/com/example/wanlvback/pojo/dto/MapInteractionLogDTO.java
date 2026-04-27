package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 地图交互日志数据传输对象
 */
@Data
public class MapInteractionLogDTO implements Serializable {

    private Long id; // 主键ID

    private Long userId; // 用户ID

    private String sessionId; // 会话ID

    private Long scenicAreaId; // 景区ID

    private Long spotId; // 景点ID

    private Long routeId; // 路线ID

    private String actionType; // 操作类型

    private String actionSource; // 触发来源

    private String agentResultJson; // Agent返回结果JSON

    private String remark; // 备注
}
