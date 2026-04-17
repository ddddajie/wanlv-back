package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 会话日报总结触发参数。
 */
@Data
public class SessionAnalysisTriggerDTO implements Serializable {

    /**
     * 需要分析的普通用户 ID。
     * 单会话分析时必填，批量日报分析时可以为空。
     */
    private Long userId;

    /**
     * 需要生成日报的日期，为空时默认当天。
     */
    private LocalDate reportDate;

    /**
     * 超级管理员账号。
     * 由于当前项目尚未接入统一登录拦截，因此这里沿用显式账号密码校验。
     */
    private String operatorUsername;

    /**
     * 超级管理员密码。
     */
    private String operatorPassword;

    /**
     * 是否强制重新生成日报。
     * true 表示即使当天已生成过，也重新调用 Agent 覆盖结果。
     */
    private Boolean forceReanalyze;
}
