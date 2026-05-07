package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AgentReservationOrderDTO implements Serializable {
    private Long userId;
    private Long slotId;
    private Integer visitorCount;
    private List<ReservationVisitorDTO> visitors;
    private String agentSessionCode;
    private String clientRequestId;
    private String remark;
}
