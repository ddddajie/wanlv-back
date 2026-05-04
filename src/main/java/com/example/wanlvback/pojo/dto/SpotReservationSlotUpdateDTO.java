package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpotReservationSlotUpdateDTO implements Serializable {

    private Integer totalCapacity;
    private Integer status;
    private String remark;
    private Long updateBy;
}
