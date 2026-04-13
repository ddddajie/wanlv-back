package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 后端触发会话分析请求参数
 */
@Data
public class SessionAnalysisTriggerDTO implements Serializable {

    private Long userId; // 用户 ID

    private LocalDate reportDate; // 分析日期，为空时默认当天
}
