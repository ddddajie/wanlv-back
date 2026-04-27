package com.example.wanlvback.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段结构返回对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchemaColumnVO {

    private Integer ordinalPosition; // 字段顺序

    private String columnName; // 字段名

    private String fieldName; // Java 风格字段名

    private String dataType; // MySQL 数据类型

    private String columnType; // MySQL 完整列类型

    private String javaType; // 推断 Java 类型

    private Boolean nullable; // 是否可空

    private Boolean primaryKey; // 是否主键

    private String defaultValue; // 默认值

    private String extra; // 扩展信息

    private String columnComment; // 字段注释
}
