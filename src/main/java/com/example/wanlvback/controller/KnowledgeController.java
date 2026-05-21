package com.example.wanlvback.controller;

import com.example.wanlvback.pojo.vo.KnowledgeTrainResponseVO;
import com.example.wanlvback.pojo.vo.KnowledgeUploadResponseVO;
import com.example.wanlvback.result.Result;
import com.example.wanlvback.service.KnowledgeService;
import com.example.wanlvback.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库管理控制器。
 */
@RestController
@RequestMapping("/knowledge")
@Slf4j
public class KnowledgeController {

    @Autowired
    private KnowledgeService knowledgeService;

    /**
     * 上传知识库文档。
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<KnowledgeUploadResponseVO> upload(@RequestParam(value = "scenicAreaId", required = false) Long scenicAreaId,
                                                    @RequestParam("files") MultipartFile[] files) {
        AuthUtil.requireSuperAdmin();
        log.info("收到知识库文档上传请求，scenicAreaId={}, fileCount={}", scenicAreaId, files == null ? 0 : files.length);
        return Result.success(knowledgeService.uploadKnowledgeFiles(scenicAreaId, files));
    }

    /**
     * 训练知识库。
     */
    @PostMapping("/train")
    public Result<KnowledgeTrainResponseVO> train() {
        AuthUtil.requireSuperAdmin();
        log.info("收到知识库训练请求");
        return Result.success(knowledgeService.trainKnowledge());
    }
}
