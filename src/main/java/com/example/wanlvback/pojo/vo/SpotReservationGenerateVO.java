package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class SpotReservationGenerateVO implements Serializable {

    private Integer generatedCount;
    private Integer skipCount;
}
