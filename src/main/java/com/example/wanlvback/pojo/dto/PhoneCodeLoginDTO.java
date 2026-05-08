package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 手机验证码登录请求参数
 */
@Data
public class PhoneCodeLoginDTO implements Serializable {

    private String phone; // 手机号

    private String code; // 验证码
}
