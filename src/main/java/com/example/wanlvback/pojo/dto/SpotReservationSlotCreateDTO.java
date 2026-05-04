package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SpotReservationSlotCreateDTO implements Serializable {

    private Long scenicAreaId;
    private Long spotId;
    private LocalDate visitDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer totalCapacity;
    private String remark;
    private Long createBy;
}
