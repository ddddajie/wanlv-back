package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RealNameVerifyDTO implements Serializable {

    private Long userId;
    private String realName;
    private String idCardNo;
}
