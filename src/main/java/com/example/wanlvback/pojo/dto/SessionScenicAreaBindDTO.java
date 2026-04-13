package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 会话景区绑定/确认请求参数
 */
@Data
public class SessionScenicAreaBindDTO implements Serializable {

    private Long userId; // 当前用户 ID

    private Long scenicAreaId; // 要绑定的景区 ID

    private String scenicAreaSource; // 景区来源：USER_CONFIRMED/FRONTEND/AGENT_INFERRED

    private Integer scenicAreaConfirmed; // 是否已确认：0/1

    private String sessionType; // 绑定后的会话类型，通常为 SCENIC_SERVICE
}
