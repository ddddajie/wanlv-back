package com.example.wanlvback.handler;

import com.example.wanlvback.exception.BaseException;
import com.example.wanlvback.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 全局异常处理器。
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final Pattern DUPLICATE_ENTRY_PATTERN = Pattern.compile("Duplicate entry '(.+?)'");

    @ExceptionHandler(BaseException.class)
    public Result<String> handleBaseException(BaseException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("参数异常: {}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> handleSqlIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException ex) {
        log.warn("数据库约束异常: {}", ex.getMessage());
        return Result.error(resolveDuplicateMessage(ex.getMessage()));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<String> handleDuplicateKeyException(DuplicateKeyException ex) {
        log.warn("数据库唯一键冲突: {}", ex.getMessage());
        return Result.error(resolveDuplicateMessage(ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("请求体解析失败: {}", ex.getMessage());
        return Result.error("请求参数格式错误");
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception ex) {
        log.error("系统异常", ex);
        return Result.error("系统繁忙，请稍后重试");
    }

    private String resolveDuplicateMessage(String message) {
        if (message == null) {
            return "数据已存在";
        }

        Matcher matcher = DUPLICATE_ENTRY_PATTERN.matcher(message);
        if (matcher.find()) {
            return matcher.group(1) + "已存在";
        }

        return "数据已存在";
    }
}
