package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class SpotReservationRuleVO implements Serializable {

    private Long id;
    private Long scenicAreaId;
    private Long spotId;
    private String spotName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer totalCapacity;
    private String weekDays;
    private Integer advanceDays;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
