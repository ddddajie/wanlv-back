package com.example.wanlvback.task;

import com.example.wanlvback.service.SpotReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 景点预约过期检测定时任务。
 */
@Component
@Slf4j
public class SpotReservationExpireScheduleTask {

    @Autowired
    private SpotReservationService spotReservationService;

    /**
     * 定时扫描预约结束时间已早于当前时间的订单，并更新为已过期。
     */
    @Scheduled(cron = "${wanlv.reservation.expire-cron:0 */5 * * * ?}",
            zone = "${wanlv.reservation.expire-zone:Asia/Shanghai}")
    public void expireOverdueOrders() {
        log.info("开始执行景点预约过期检测定时任务");
        int expiredCount = spotReservationService.expireOverdueOrders();
        log.info("景点预约过期检测定时任务执行完成，expiredCount={}", expiredCount);
    }
}
