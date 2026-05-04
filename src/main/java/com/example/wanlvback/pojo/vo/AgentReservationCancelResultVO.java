package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class AgentReservationCancelResultVO implements Serializable {

    private Boolean success;
    private String reservationNo;
    private String replyText;
}
