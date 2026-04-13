package com.example.wanlvback.service;

import com.example.wanlvback.pojo.dto.ChatAskDTO;
import com.example.wanlvback.pojo.dto.SessionAnalysisTriggerDTO;
import com.example.wanlvback.pojo.dto.SessionScenicAreaBindDTO;
import com.example.wanlvback.pojo.vo.AgentSessionAnalysisVO;
import com.example.wanlvback.pojo.vo.ChatAnswerVO;

/**
 * 聊天业务接口。
 */
public interface ChatService {

    /**
     * 用户提问并获取 Agent 回复。
     *
     * @param chatAskDTO 聊天提问参数
     * @return 聊天回复结果
     */
    ChatAnswerVO ask(ChatAskDTO chatAskDTO);

    /**
     * 对指定会话执行分析。
     *
     * @param triggerDTO 会话分析触发参数
     * @return 会话分析结果
     */
    AgentSessionAnalysisVO analyzeSession(SessionAnalysisTriggerDTO triggerDTO);

    /**
     * 绑定会话对应的景区信息。
     *
     * @param bindDTO 景区绑定参数
     * @return 会话主键 ID
     */
    Long bindScenicArea(SessionScenicAreaBindDTO bindDTO);
}
