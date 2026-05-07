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

    private Integer realNameStatus; // 实名状态：0未实名，1已实名，2实名失败

    private String realName; // 真实姓名

    private String idCardMasked; // 脱敏身份证号

    private String idCardHash; // 身份证哈希，用于防重复预约

    private LocalDateTime realNameTime; // 实名通过时间

    private LocalDateTime lastLoginTime; // 最后登录时间

    private Integer deleted; // 删除标记

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间
}
