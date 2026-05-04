package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AgentReservationOrderDTO implements Serializable {
    private Long userId;
    private Long slotId;
    private Integer visitorCount;
    private String agentSessionCode;
    private String clientRequestId;
    private String remark;
}
