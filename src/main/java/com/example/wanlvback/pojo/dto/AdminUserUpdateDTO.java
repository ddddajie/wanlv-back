package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员用户更新请求参数
 */
@Data
public class AdminUserUpdateDTO implements Serializable {

    private Long id; // 管理员 ID

    private String username; // 账号

    private String password; // 密码

    private String realName; // 真实姓名

    private String phone; // 手机号

    private String email; // 邮箱

    private String avatarUrl; // 头像地址

    private String role; // 角色

    private String scenicSpot; // 所属景区

    private Integer status; // 状态

    private String remark; // 备注
}
