package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class SpotReservationSlotVO implements Serializable {

    private Long id;
    private Long slotId;
    private Long scenicAreaId;
    private String scenicName;
    private Long spotId;
    private String spotName;
    private Long ruleId;
    private LocalDate visitDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer totalCapacity;
    private Integer reservedCount;
    private Integer remainingCount;
    private Boolean available;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
