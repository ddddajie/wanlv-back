package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class AgentReservationSpotVO implements Serializable {

    private Long spotId;
    private String spotName;
    private Long scenicAreaId;
    private String scenicName;
    private Integer reservationEnabled;
    private String reservationNotice;
    private Integer advanceReservationDays;
    private Integer minAdvanceMinutes;
    private String matchType;
    private Double confidence;
}
