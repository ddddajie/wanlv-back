package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 普通用户更新请求参数
 */
@Data
public class NormalUserUpdateDTO implements Serializable {

    private Long id; // 用户 ID

    private String username; // 账号

    private String password; // 密码

    private String nickname; // 昵称

    private String phone; // 手机号

    private String email; // 邮箱

    private String avatarUrl; // 头像地址

    private Integer gender; // 性别

    private Integer age; // 年龄

    private String interestTags; // 兴趣标签 JSON

    private Integer status; // 状态
}
