package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReservationVisitorDTO implements Serializable {

    private String realName;
    private String idCardNo;
    private Boolean booker;
}
