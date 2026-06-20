package com.example.wanlvback.exception;

/**
 * 登录状态无效异常。
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
