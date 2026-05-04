package com.example.wanlvback.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotReservationOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String reservationNo;
    private Long userId;
    private Long scenicAreaId;
    private Long spotId;
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
