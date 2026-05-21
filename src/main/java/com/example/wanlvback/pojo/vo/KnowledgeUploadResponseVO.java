package com.example.wanlvback.pojo.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 知识库文档上传响应。
 */
@Data
public class KnowledgeUploadResponseVO implements Serializable {

    private Integer code; // Agent 返回码

    private String message; // Agent 返回消息

    @JSONField(name = "saved_files")
    @JsonProperty("saved_files")
    private List<String> savedFiles; // 成功保存的文件名列表

    @JSONField(name = "rejected_files")
    @JsonProperty("rejected_files")
    private List<String> rejectedFiles; // 被过滤或拒绝的文件名列表
}
