package com.example.wanlvback.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 普通用户 refreshToken 持久化记录。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysNormalUserRefreshToken implements Serializable {

    private Long id;

    private String tokenHash; // 仅保存摘要，数据库泄露时不能直接拿来刷新

    private Long userId;

    private LocalDateTime expireTime;

    private Integer invalidated; // 0有效，1已失效
}
