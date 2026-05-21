package com.example.wanlvback.service;

import com.example.wanlvback.pojo.vo.KnowledgeTrainResponseVO;
import com.example.wanlvback.pojo.vo.KnowledgeUploadResponseVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库管理业务接口。
 */
public interface KnowledgeService {

    /**
     * 上传知识库文档到 Agent。
     */
    KnowledgeUploadResponseVO uploadKnowledgeFiles(Long scenicAreaId, MultipartFile[] files);

    /**
     * 触发 Agent 训练知识库。
     */
    KnowledgeTrainResponseVO trainKnowledge();
}
