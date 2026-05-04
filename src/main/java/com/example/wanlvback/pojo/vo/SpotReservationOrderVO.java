package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class SpotReservationOrderVO implements Serializable {

    private Long id;
    private String reservationNo;
    private Long userId;
    private String nickname;
    private Long scenicAreaId;
    private String scenicName;
    private Long spotId;
    private String spotName;
    private Long slotId;
    private LocalDate visitDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer visitorCount;
    private String contactName;
    private String contactPhone;
    private String status;
    private String sourceType;
    private String agentSessionCode;
    private String clientRequestId;
    private String remark;
    private String cancelReason;
    private LocalDateTime cancelTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
