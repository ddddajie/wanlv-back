package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalTime;

@Data
public class SpotReservationRuleUpdateDTO implements Serializable {

    private LocalTime startTime;
    private LocalTime endTime;
    private Integer totalCapacity;
    private String weekDays;
    private Integer advanceDays;
    private Integer status;
    private String remark;
    private Long updateBy;
}
