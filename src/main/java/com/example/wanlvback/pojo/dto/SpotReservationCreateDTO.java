package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SpotReservationCreateDTO implements Serializable {

    private Long userId;
    private Long slotId;
    private Integer visitorCount;
    private List<ReservationVisitorDTO> visitors;
    private String contactName;
    private String contactPhone;
    private String sourceType;
    private String agentSessionCode;
    private String clientRequestId;
    private String remark;
}
