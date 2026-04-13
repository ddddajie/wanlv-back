package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 新增管理员请求参数
 */
@Data
public class AdminCreateDTO implements Serializable {

    private String operatorUsername; // 操作人账号

    private String operatorPassword; // 操作人密码

    private String username; // 新管理员账号

    private String password; // 新管理员密码

    private String realName; // 真实姓名

    private String phone; // 手机号

    private String email; // 邮箱

    private String avatarUrl; // 头像地址

    private String scenicSpot; // 所属景区

    private String remark; // 备注
}
