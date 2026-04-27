package com.example.wanlvback.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 表结构返回对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchemaTableVO {

    private String tableName; // 表名

    private String tableComment; // 表注释

    private List<SchemaColumnVO> columns; // 字段列表
}
