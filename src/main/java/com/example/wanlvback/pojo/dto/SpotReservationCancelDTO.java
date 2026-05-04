package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpotReservationCancelDTO implements Serializable {

    private Long userId;
    private String cancelReason;
}
