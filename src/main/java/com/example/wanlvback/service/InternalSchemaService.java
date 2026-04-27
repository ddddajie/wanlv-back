package com.example.wanlvback.service;

import com.example.wanlvback.pojo.vo.DatabaseSchemaVO;

import java.util.List;

/**
 * 内部表结构服务
 */
public interface InternalSchemaService {

    /**
     * 查询数据库表结构
     *
     * @param tableNames 指定表名，可为空
     * @return 表结构信息
     */
    DatabaseSchemaVO getSchema(List<String> tableNames);
}
