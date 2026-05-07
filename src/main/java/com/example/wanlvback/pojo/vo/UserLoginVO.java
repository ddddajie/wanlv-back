package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户登录返回对象
 */
@Data
@Builder
public class UserLoginVO implements Serializable {

    private Long id; // 用户 ID

    private String username; // 账号

    private String displayName; // 展示名称

    private String userType; // 用户类型

    private String role; // 角色

    private Integer status; // 状态

    private Integer realNameStatus; // 实名状态：0未实名，1已实名，2实名失败

    private LocalDateTime lastLoginTime; // 最后登录时间
}
