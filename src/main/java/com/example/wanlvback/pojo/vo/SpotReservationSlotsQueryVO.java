package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class SpotReservationSlotsQueryVO implements Serializable {

    private Long spotId;
    private String spotName;
    private LocalDate visitDate;
    private List<SpotReservationSlotVO> slots;
}
