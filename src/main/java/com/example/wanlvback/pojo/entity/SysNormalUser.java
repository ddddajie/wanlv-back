package com.example.wanlvback.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 普通用户实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysNormalUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id; // 主键 ID

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

    private LocalDateTime lastLoginTime; // 最后登录时间

    private Integer deleted; // 删除标记

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间
}
