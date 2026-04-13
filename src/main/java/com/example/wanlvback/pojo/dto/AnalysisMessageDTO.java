package com.example.wanlvback.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 会话分析消息项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisMessageDTO implements Serializable {

    private String role; // 消息角色：user/assistant

    private String content; // 消息内容
}
