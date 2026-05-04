package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpotReservationRuleStatusDTO implements Serializable {

    private Integer status;
    private Long updateBy;
}
