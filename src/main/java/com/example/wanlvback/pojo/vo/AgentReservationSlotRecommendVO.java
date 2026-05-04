package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class AgentReservationSlotRecommendVO implements Serializable {

    private List<SpotReservationSlotVO> recommendedSlots;
    private String replyText;
}
