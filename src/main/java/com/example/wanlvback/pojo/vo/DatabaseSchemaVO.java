package com.example.wanlvback.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据库表结构返回对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseSchemaVO {

    private String databaseName; // 数据库名

    private LocalDateTime generatedAt; // 生成时间

    private List<SchemaTableVO> tables; // 表结构列表
}
