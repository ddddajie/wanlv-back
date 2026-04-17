package com.example.wanlvback.task;

import com.example.wanlvback.pojo.vo.SessionAnalysisBatchVO;
import com.example.wanlvback.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 会话日报总结定时任务。
 */
@Component
@Slf4j
public class SessionAnalysisScheduleTask {

    @Autowired
    private ChatService chatService;

    @Value("${wanlv.agent.daily-analysis-zone:Asia/Shanghai}")
    private String dailyAnalysisZone;

    /**
     * 每天临近当天结束时自动跑一次日报总结。
     * 核心逻辑仍然走 ChatService，避免接口和定时任务逻辑分叉。
     */
    @Scheduled(cron = "${wanlv.agent.daily-analysis-cron:59 59 23 * * ?}",
            zone = "${wanlv.agent.daily-analysis-zone:Asia/Shanghai}")
    public void analyzeDailySessions() {
        LocalDate reportDate = LocalDate.now(ZoneId.of(dailyAnalysisZone));
        log.info("开始执行会话日报总结定时任务，reportDate={}", reportDate);

        SessionAnalysisBatchVO batchVO = chatService.analyzeDailySessions(reportDate, false);
        log.info("会话日报总结定时任务执行完成，reportDate={}, totalCount={}, successCount={}, skippedCount={}, failedCount={}",
                reportDate,
                batchVO.getTotalCount(),
                batchVO.getSuccessCount(),
                batchVO.getSkippedCount(),
                batchVO.getFailedCount());
    }
}
