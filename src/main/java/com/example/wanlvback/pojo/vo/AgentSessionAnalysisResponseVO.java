package com.example.wanlvback.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * Agent 会话分析响应结果
 */
@Data
public class AgentSessionAnalysisResponseVO implements Serializable {

    private Integer code; // Agent 返回码

    private String message; // Agent 返回信息

    private AgentSessionAnalysisVO analysis; // 会话分析结果
}
