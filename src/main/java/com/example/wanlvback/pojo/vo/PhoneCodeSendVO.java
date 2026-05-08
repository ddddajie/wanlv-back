package com.example.wanlvback.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 手机验证码发送返回对象
 */
@Data
@Builder
public class PhoneCodeSendVO implements Serializable {

    private String phone; // 手机号

    private String code; // 验证码，模拟短信阶段返回给前端联调

    private Long expireSeconds; // 有效秒数

    private LocalDateTime expireTime; // 过期时间
}
