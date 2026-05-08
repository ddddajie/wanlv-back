package com.example.wanlvback.controller;

import com.example.wanlvback.pojo.dto.ChatAskDTO;
import com.example.wanlvback.pojo.dto.SessionAnalysisTriggerDTO;
import com.example.wanlvback.pojo.dto.SessionScenicAreaBindDTO;
import com.example.wanlvback.pojo.vo.AgentSessionAnalysisVO;
import com.example.wanlvback.pojo.vo.ChatAnswerVO;
import com.example.wanlvback.pojo.vo.SessionAnalysisBatchVO;
import com.example.wanlvback.result.Result;
import com.example.wanlvback.service.ChatService;
import com.example.wanlvback.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 聊天与会话分析控制器。
 */
@RestController
@RequestMapping("/agent")
@Slf4j
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 处理用户向 Agent 发起的聊天请求。
     */
    @PostMapping("/chat")
    public Result<ChatAnswerVO> chat(@RequestBody ChatAskDTO chatAskDTO) {
        AuthUtil.requireSelf(chatAskDTO == null ? null : chatAskDTO.getUserId());
        log.info("收到 Agent 聊天请求，userId={}", chatAskDTO.getUserId());
        return Result.success(chatService.ask(chatAskDTO));
    }

    /**
     * 手动触发单个用户某一天的日报总结。
     * 该接口仅允许超级管理员调用。
     */
    @PostMapping("/session-analysis")
    public Result<AgentSessionAnalysisVO> analyzeSession(@RequestBody SessionAnalysisTriggerDTO triggerDTO) {
        AuthUtil.requireSuperAdmin();
        log.info("收到单会话日报总结请求，operator={}, userId={}, reportDate={}",
                triggerDTO == null ? null : triggerDTO.getOperatorUsername(),
                triggerDTO == null ? null : triggerDTO.getUserId(),
                triggerDTO == null ? null : triggerDTO.getReportDate());
        return Result.success(chatService.analyzeSession(triggerDTO));
    }

    /**
     * 按日报日期批量触发当天所有会话的总结。
     * 该接口仅允许超级管理员调用，定时任务也会复用同一套服务逻辑。
     */
    @PostMapping("/session-analysis/daily")
    public Result<SessionAnalysisBatchVO> analyzeDailySessions(@RequestBody SessionAnalysisTriggerDTO triggerDTO) {
        AuthUtil.requireSuperAdmin();
        log.info("收到日报批量总结请求，operator={}, reportDate={}, forceReanalyze={}",
                triggerDTO == null ? null : triggerDTO.getOperatorUsername(),
                triggerDTO == null ? null : triggerDTO.getReportDate(),
                triggerDTO == null ? null : triggerDTO.getForceReanalyze());
        return Result.success(chatService.analyzeDailySessions(triggerDTO));
    }

    /**
     * 将当前会话与景区绑定。
     */
    @PostMapping("/session/scenic-area/bind")
    public Result<Long> bindScenicArea(@RequestBody SessionScenicAreaBindDTO bindDTO) {
        AuthUtil.requireSelf(bindDTO == null ? null : bindDTO.getUserId());
        log.info("收到景区绑定请求，userId={}, scenicAreaId={}", bindDTO.getUserId(), bindDTO.getScenicAreaId());
        return Result.success(chatService.bindScenicArea(bindDTO));
    }
}
