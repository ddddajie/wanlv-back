package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ReservationVisitorVO implements Serializable {

    private String realName;
    private String idCardMasked;
    private Boolean booker;
    private String status;
}
