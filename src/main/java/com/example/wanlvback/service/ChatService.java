package com.example.wanlvback.service;

import com.example.wanlvback.pojo.dto.ChatAskDTO;
import com.example.wanlvback.pojo.dto.SessionAnalysisTriggerDTO;
import com.example.wanlvback.pojo.dto.SessionScenicAreaBindDTO;
import com.example.wanlvback.pojo.vo.AgentSessionAnalysisVO;
import com.example.wanlvback.pojo.vo.ChatAnswerVO;
import com.example.wanlvback.pojo.vo.SessionAnalysisBatchVO;

import java.time.LocalDate;

/**
 * 聊天业务接口。
 */
public interface ChatService {

    /**
     * 用户提问并获取 Agent 回复。
     */
    ChatAnswerVO ask(ChatAskDTO chatAskDTO);

    /**
     * 对指定用户指定日期的会话生成日报总结。
     */
    AgentSessionAnalysisVO analyzeSession(SessionAnalysisTriggerDTO triggerDTO);

    /**
     * 按日期批量生成日报总结。
     * 该方法供超级管理员接口调用。
     */
    SessionAnalysisBatchVO analyzeDailySessions(SessionAnalysisTriggerDTO triggerDTO);

    /**
     * 按日期批量生成日报总结。
     * 该方法供定时任务直接调用，不做管理员身份校验。
     */
    SessionAnalysisBatchVO analyzeDailySessions(LocalDate reportDate, boolean forceReanalyze);

    /**
     * 绑定会话对应的景区信息。
     */
    Long bindScenicArea(SessionScenicAreaBindDTO bindDTO);
}
