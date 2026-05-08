package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 手机验证码发送请求参数
 */
@Data
public class PhoneCodeSendDTO implements Serializable {

    private String phone; // 手机号
}
