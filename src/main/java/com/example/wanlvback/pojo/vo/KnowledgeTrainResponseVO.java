package com.example.wanlvback.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 知识库训练响应。
 */
@Data
public class KnowledgeTrainResponseVO implements Serializable {

    private Integer code; // Agent 返回码

    private String message; // Agent 返回消息
}
