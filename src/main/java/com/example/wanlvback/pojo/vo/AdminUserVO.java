package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员用户详情返回对象
 */
@Data
@Builder
public class AdminUserVO implements Serializable {

    private Long id; // 管理员 ID

    private String username; // 账号

    private String realName; // 真实姓名

    private String phone; // 手机号

    private String email; // 邮箱

    private String avatarUrl; // 头像地址

    private String role; // 角色

    private String scenicSpot; // 所属景区

    private Integer status; // 状态

    private LocalDateTime lastLoginTime; // 最后登录时间

    private String remark; // 备注

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间
}
