package com.example.wanlvback.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 按日期批量生成日报的返回结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionAnalysisBatchVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 生成的日报日期。
     */
    private LocalDate reportDate;

    /**
     * 当天命中的会话总数。
     */
    private Integer totalCount;

    /**
     * 成功生成日报的会话数。
     */
    private Integer successCount;

    /**
     * 已有日报因此跳过的会话数。
     */
    private Integer skippedCount;

    /**
     * 生成失败的会话数。
     */
    private Integer failedCount;

    /**
     * 每条会话的执行明细。
     */
    private List<SessionAnalysisBatchItemVO> items;
}
