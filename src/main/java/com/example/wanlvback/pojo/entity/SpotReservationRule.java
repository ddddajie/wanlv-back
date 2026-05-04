package com.example.wanlvback.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotReservationRule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long scenicAreaId;
    private Long spotId;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer totalCapacity;
    private String weekDays;
    private Integer advanceDays;
    private Integer status;
    private String remark;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}
