package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Token 刷新结果。
 */
@Data
@Builder
public class TokenRefreshVO implements Serializable {

    private String token;

    private String refreshToken;

    private Long expireSeconds;

    private Long refreshExpireSeconds;
}
