package com.example.wanlvback.service.impl;

import com.example.wanlvback.exception.BaseException;
import com.example.wanlvback.mapper.KnowledgeDocumentMapper;
import com.example.wanlvback.pojo.entity.KnowledgeDocument;
import com.example.wanlvback.pojo.vo.KnowledgeTrainResponseVO;
import com.example.wanlvback.pojo.vo.KnowledgeUploadResponseVO;
import com.example.wanlvback.service.KnowledgeService;
import com.example.wanlvback.utils.AgentChatHttpUtil;
import com.example.wanlvback.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

/**
 * 知识库管理业务实现。
 */
@Service
@Slf4j
public class KnowledgeServiceImpl implements KnowledgeService {

    private static final long GLOBAL_SCENIC_AREA_ID = 0L;
    private static final String PARSE_STATUS_PENDING = "pending";
    private static final String PUBLISH_STATUS_DRAFT = "draft";

    @Autowired
    private AgentChatHttpUtil agentChatHttpUtil;

    @Autowired
    private KnowledgeDocumentMapper knowledgeDocumentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeUploadResponseVO uploadKnowledgeFiles(Long scenicAreaId, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BaseException("请至少上传一个知识库文档");
        }
        boolean hasAvailableFile = false;
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                hasAvailableFile = true;
                break;
            }
        }
        if (!hasAvailableFile) {
            throw new BaseException("上传的知识库文档不能为空");
        }

        Long actualScenicAreaId = scenicAreaId == null ? GLOBAL_SCENIC_AREA_ID : scenicAreaId;
        log.info("准备转发知识库文档上传请求，scenicAreaId={}, fileCount={}", actualScenicAreaId, files.length);
        KnowledgeUploadResponseVO responseVO = agentChatHttpUtil.uploadKnowledgeFiles(files);
        saveUploadedDocuments(actualScenicAreaId, files, responseVO);
        return responseVO;
    }

    @Override
    public KnowledgeTrainResponseVO trainKnowledge() {
        log.info("准备转发知识库训练请求");
        return agentChatHttpUtil.trainKnowledge();
    }

    /**
     * 保存 Agent 已接收的知识库文档记录，未通过类型过滤的 rejected_files 不入库。
     */
    private void saveUploadedDocuments(Long scenicAreaId, MultipartFile[] files, KnowledgeUploadResponseVO responseVO) {
        List<String> savedFiles = responseVO.getSavedFiles();
        if (savedFiles == null || savedFiles.isEmpty()) {
            return;
        }

        Queue<MultipartFile> acceptedFiles = buildAcceptedFileQueue(files, responseVO.getRejectedFiles());
        LocalDateTime now = LocalDateTime.now();
        Long uploadedBy = AuthUtil.getCurrentUserId();
        for (String savedFileName : savedFiles) {
            MultipartFile sourceFile = acceptedFiles.poll();
            String originalFilename = sourceFile == null ? savedFileName : sourceFile.getOriginalFilename();
            KnowledgeDocument knowledgeDocument = KnowledgeDocument.builder()
                    // 重点：上传接口未强制传景区时，使用 0 表示全局知识库文档。
                    .scenicAreaId(scenicAreaId)
                    .docName(resolveDocName(originalFilename))
                    .fileName(originalFilename)
                    .fileSize(sourceFile == null ? null : sourceFile.getSize())
                    .fileSuffix(resolveFileSuffix(originalFilename))
                    .parseStatus(PARSE_STATUS_PENDING)
                    .publishStatus(PUBLISH_STATUS_DRAFT)
                    .uploadedBy(uploadedBy)
                    .deleted(0)
                    .createTime(now)
                    .build();
            knowledgeDocumentMapper.insert(knowledgeDocument);
        }
    }

    private Queue<MultipartFile> buildAcceptedFileQueue(MultipartFile[] files, List<String> rejectedFiles) {
        Map<String, Integer> rejectedFileCount = new HashMap<>();
        if (rejectedFiles != null) {
            for (String rejectedFile : rejectedFiles) {
                rejectedFileCount.merge(rejectedFile, 1, Integer::sum);
            }
        }

        Queue<MultipartFile> acceptedFiles = new LinkedList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String originalFilename = file.getOriginalFilename();
            if (!StringUtils.hasText(originalFilename)) {
                continue;
            }

            if (isRejectedOriginalFile(originalFilename, rejectedFileCount)) {
                continue;
            }
            // 重点：Agent 可能会给 saved_files 改名编号，入库必须保留这里的原始上传文件名。
            acceptedFiles.offer(file);
        }
        return acceptedFiles;
    }

    private boolean isRejectedOriginalFile(String originalFilename, Map<String, Integer> rejectedFileCount) {
        if (rejectedFileCount == null || rejectedFileCount.isEmpty()) {
            return false;
        }
        Integer count = rejectedFileCount.get(originalFilename);
        if (count == null || count <= 0) {
            return false;
        }
        if (count == 1) {
            rejectedFileCount.remove(originalFilename);
        } else {
            rejectedFileCount.put(originalFilename, count - 1);
        }
        return true;
    }

    private String resolveDocName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "知识库文档";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }

    private String resolveFileSuffix(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return null;
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
