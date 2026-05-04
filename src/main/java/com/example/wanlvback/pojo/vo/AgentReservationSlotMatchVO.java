package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class AgentReservationSlotMatchVO implements Serializable {

    private SpotReservationSlotVO matchedSlot;
    private List<SpotReservationSlotVO> candidateSlots;
    private String matchType;
    private String replyText;
}
