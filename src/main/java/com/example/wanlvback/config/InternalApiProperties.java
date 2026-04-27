package com.example.wanlvback.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 内部接口配置
 */
@Data
@ConfigurationProperties(prefix = "wanlv.internal")
public class InternalApiProperties {

    /**
     * 是否启用内部接口
     */
    private boolean enabled = true;

    /**
     * 内部接口访问令牌
     */
    private String token = "change-this-internal-token";

    /**
     * 令牌请求头名称
     */
    private String headerName = "X-Internal-Token";

    /**
     * 是否允许本机地址访问
     */
    private boolean allowLoopback = true;

    /**
     * 是否允许内网地址访问
     */
    private boolean allowPrivateNetwork = true;
}
