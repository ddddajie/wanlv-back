package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.KnowledgeDocument;

/**
 * 知识库文档数据访问层。
 */
public interface KnowledgeDocumentMapper {

    /**
     * 新增知识库文档上传记录。
     */
    int insert(KnowledgeDocument knowledgeDocument);
}
