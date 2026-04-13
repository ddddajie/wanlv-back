package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 普通用户登录请求参数
 */
@Data
public class NormalUserLoginDTO implements Serializable {

    private String username; // 账号
    private String password; // 密码
}
