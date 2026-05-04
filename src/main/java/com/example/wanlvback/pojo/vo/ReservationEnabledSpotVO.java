package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ReservationEnabledSpotVO implements Serializable {

    private Long spotId;
    private Long scenicAreaId;
    private String spotName;
    private String shortIntro;
    private Integer reservationEnabled;
    private String reservationNotice;
    private Integer advanceReservationDays;
    private Integer minAdvanceMinutes;
}
