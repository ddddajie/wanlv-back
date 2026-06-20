package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * refreshToken 请求参数。
 */
@Data
public class RefreshTokenDTO implements Serializable {

    private String refreshToken;
}
