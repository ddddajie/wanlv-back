package com.example.wanlvback.controller;

import com.example.wanlvback.pojo.dto.ChatAskDTO;
import com.example.wanlvback.pojo.dto.SessionAnalysisTriggerDTO;
import com.example.wanlvback.pojo.dto.SessionScenicAreaBindDTO;
import com.example.wanlvback.pojo.vo.AgentSessionAnalysisVO;
import com.example.wanlvback.pojo.vo.ChatAnswerVO;
import com.example.wanlvback.result.Result;
import com.example.wanlvback.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent 聊天控制器
 */
@RestController
@RequestMapping("/agent")
@Slf4j
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public Result<ChatAnswerVO> chat(@RequestBody ChatAskDTO chatAskDTO) {
        log.info("收到Agent聊天请求，userId={}", chatAskDTO.getUserId());
        return Result.success(chatService.ask(chatAskDTO));
    }

    @PostMapping("/session-analysis")
    public Result<AgentSessionAnalysisVO> analyzeSession(@RequestBody SessionAnalysisTriggerDTO triggerDTO) {
        log.info("收到会话分析请求，userId={}, reportDate={}", triggerDTO.getUserId(), triggerDTO.getReportDate());
        return Result.success(chatService.analyzeSession(triggerDTO));
    }

    @PostMapping("/session/scenic-area/bind")
    public Result<Long> bindScenicArea(@RequestBody SessionScenicAreaBindDTO bindDTO) {
        log.info("收到景区绑定请求，userId={}, scenicAreaId={}", bindDTO.getUserId(), bindDTO.getScenicAreaId());
        return Result.success(chatService.bindScenicArea(bindDTO));
    }
}
