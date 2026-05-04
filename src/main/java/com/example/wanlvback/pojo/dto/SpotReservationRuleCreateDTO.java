package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalTime;

@Data
public class SpotReservationRuleCreateDTO implements Serializable {

    private Long scenicAreaId;
    private Long spotId;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer totalCapacity;
    private String weekDays;
    private Integer advanceDays;
    private String remark;
    private Long createBy;
}
