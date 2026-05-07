package com.example.wanlvback.pojo.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 普通用户详情返回对象
 */
@Data
@Builder
public class NormalUserVO implements Serializable {

    private Long id; // 用户 ID

    private String username; // 账号

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

    private LocalDateTime realNameTime; // 实名通过时间

    private LocalDateTime lastLoginTime; // 最后登录时间

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间
}
