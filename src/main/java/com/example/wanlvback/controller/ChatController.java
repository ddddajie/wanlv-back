package com.example.wanlvback.controller;

import com.example.wanlvback.pojo.dto.ChatAskDTO;
import com.example.wanlvback.pojo.dto.SessionAnalysisTriggerDTO;
import com.example.wanlvback.pojo.dto.SessionScenicAreaBindDTO;
import com.example.wanlvback.pojo.vo.AgentSessionAnalysisVO;
import com.example.wanlvback.pojo.vo.ChatAnswerVO;
import com.example.wanlvback.result.Result;
import com.example.wanlvback.service.ChatService;
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

    /**
     * 注入聊天业务服务。
     */
    @Autowired
    private ChatService chatService;

    /**
     * 处理用户向 Agent 发起的聊天请求。
     *
     * @param chatAskDTO 前端传入的提问参数
     * @return Agent 回复结果
     */
    @PostMapping("/chat")
    public Result<ChatAnswerVO> chat(@RequestBody ChatAskDTO chatAskDTO) {
        log.info("收到 Agent 聊天请求，userId={}", chatAskDTO.getUserId());
        return Result.success(chatService.ask(chatAskDTO));
    }

    /**
     * 触发指定会话的分析流程。
     *
     * @param triggerDTO 会话分析触发参数
     * @return 会话分析结果
     */
    @PostMapping("/session-analysis")
    public Result<AgentSessionAnalysisVO> analyzeSession(@RequestBody SessionAnalysisTriggerDTO triggerDTO) {
        log.info("收到会话分析请求，userId={}, reportDate={}", triggerDTO.getUserId(), triggerDTO.getReportDate());
        return Result.success(chatService.analyzeSession(triggerDTO));
    }

    /**
     * 将当前会话与景区绑定。
     *
     * @param bindDTO 景区绑定参数
     * @return 当前会话主键 ID
     */
    @PostMapping("/session/scenic-area/bind")
    public Result<Long> bindScenicArea(@RequestBody SessionScenicAreaBindDTO bindDTO) {
        log.info("收到景区绑定请求，userId={}, scenicAreaId={}", bindDTO.getUserId(), bindDTO.getScenicAreaId());
        return Result.success(chatService.bindScenicArea(bindDTO));
    }
}
