package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员登录请求参数
 */
@Data
public class AdminLoginDTO implements Serializable {

    private String username; // 账号
    private String password; // 密码
}
