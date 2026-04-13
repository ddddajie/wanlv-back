package com.example.wanlvback.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识文档实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDocument implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id; // 主键 ID

    private Long scenicAreaId; // 景区 ID

    private String docName; // 文档名称

    private String fileName; // 原始文件名

    private Long fileSize; // 文件大小

    private String fileSuffix; // 文件后缀

    private String parseStatus; // 解析状态

    private String publishStatus; // 发布状态

    private Long uploadedBy; // 上传人 ID

    private LocalDateTime publishedTime; // 发布时间

    private String remark; // 备注

    private Integer deleted; // 删除标记

    private LocalDateTime createTime; // 创建时间
}
