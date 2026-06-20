package com.example.wanlvback.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置项。
 */
@Data
@Component
@ConfigurationProperties(prefix = "wanlv.jwt")
public class JwtProperties {

    /**
     * 重点：生产环境必须通过环境变量覆盖，长度至少 32 字节。
     */
    private String secretKey;

    /**
     * token 过期时间，单位毫秒。
     */
    private Long ttlMs;

    /**
     * refreshToken 过期时间，单位毫秒。
     */
    private Long refreshTtlMs;

    private String headerName = "Authorization";

    private String tokenPrefix = "Bearer";
}
