package com.example.wanlvback.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 单条会话日报生成结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionAnalysisBatchItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long sessionId;

    private String sessionCode;

    private Long userId;

    /**
     * 是否处理成功。
     */
    private Boolean success;

    /**
     * 是否被跳过。
     */
    private Boolean skipped;

    /**
     * 执行结果说明。
     */
    private String message;

    /**
     * 本次生成的日报摘要，便于管理端快速查看。
     */
    private String summary;
}
