package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class AgentReservationOrderResultVO implements Serializable {

    private Boolean success;
    private String reservationNo;
    private String status;
    private Long scenicAreaId;
    private String scenicName;
    private Long spotId;
    private String spotName;
    private Long slotId;
    private LocalDate visitDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer visitorCount;
    private String replyText;
}
