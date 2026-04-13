package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 前端聊天提问请求参数
 */
@Data
public class ChatAskDTO implements Serializable {

    private Long userId; // 当前提问用户 ID

    private String content; // 用户提问内容

    private String messageType; // 消息类型：text/voice/image

    private String voiceText; // 语音转文字文本

    private Long scenicAreaId; // 前端显式选择的景区 ID，可为空

    private String scenicAreaSource; // 景区来源，通常为 FRONTEND 或 USER_CONFIRMED

    private Integer scenicAreaConfirmed; // 是否已经明确确认景区：0/1

    private String sourceType; // 提问入口：GLOBAL_CHAT/SCENIC_DETAIL/ROUTE_DETAIL 等

    private String sourceId; // 入口业务主体 ID
}
