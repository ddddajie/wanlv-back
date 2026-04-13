package com.example.wanlvback.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 游客会话消息实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 数据库主键 ID。
     */
    private Long id;

    /**
     * 所属会话主键 ID。
     * 这里存的是 visitor_session.id，不是 sessionCode。
     */
    private Long sessionId;

    private Long userId;
    private Integer messageNo;
    private String senderType;
    private String messageType;
    private String content;
    private String voiceText;
    private LocalDateTime createTime;
}
