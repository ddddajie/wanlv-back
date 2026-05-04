package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpotReservationSlotGenerateDTO implements Serializable {

    private Long scenicAreaId;
    private Long spotId;
    private Integer days;
}
