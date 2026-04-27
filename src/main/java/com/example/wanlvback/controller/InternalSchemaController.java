package com.example.wanlvback.controller;

import com.example.wanlvback.pojo.vo.DatabaseSchemaVO;
import com.example.wanlvback.result.Result;
import com.example.wanlvback.service.InternalSchemaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 内部表结构接口
 */
@RestController
@RequestMapping("/internal/schema")
@Slf4j
public class InternalSchemaController {

    @Autowired
    private InternalSchemaService internalSchemaService;

    /**
     * 查询当前库表结构
     */
    @GetMapping("/tables")
    public Result<DatabaseSchemaVO> getTables(@RequestParam(required = false) List<String> tableNames) {
        log.info("收到内部表结构查询请求, tableNames={}", tableNames);
        return Result.success(internalSchemaService.getSchema(tableNames));
    }
}
