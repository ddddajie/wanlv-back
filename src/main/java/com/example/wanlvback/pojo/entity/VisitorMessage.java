package com.example.wanlvback.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 游客消息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id; // 主键 ID

    private Long sessionId; // 所属会话 ID

    private Long userId; // 所属用户 ID

    private Integer messageNo; // 会话内消息序号

    private String senderType; // 发送方类型：visitor/agent/system

    private String messageType; // 消息类型：text/voice/image/video

    private String content; // 消息内容

    private String voiceText; // 语音转写文本

    private LocalDateTime createTime; // 创建时间
}
