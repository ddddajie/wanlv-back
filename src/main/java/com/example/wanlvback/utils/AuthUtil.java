package com.example.wanlvback.utils;

import com.example.wanlvback.context.BaseContext;
import com.example.wanlvback.exception.BaseException;
import org.springframework.util.StringUtils;

public class AuthUtil {

    private AuthUtil() {
    }

    public static Long getCurrentUserId() {
        return BaseContext.getCurrentId();
    }

    public static String getCurrentUserType() {
        return BaseContext.getCurrentUserType();
    }

    public static String getCurrentRole() {
        return BaseContext.getCurrentRole();
    }

    public static boolean isAdminUser() {
        return "admin".equals(getCurrentUserType());
    }

    public static boolean isSuperAdmin() {
        return "super_admin".equals(getCurrentRole());
    }

    public static void requireLogin() {
        if (getCurrentUserId() == null) {
            throw new BaseException("请先登录");
        }
    }

    public static void requireAdmin() {
        requireLogin();
        if (!isAdminUser()) {
            throw new BaseException("仅管理员可操作");
        }
    }

    public static void requireSuperAdmin() {
        requireAdmin();
        if (!isSuperAdmin()) {
            throw new BaseException("仅超级管理员可操作");
        }
    }

    /**
     * 重点：防止前端篡改 userId 操作其他用户数据。
     */
    public static void requireSelf(Long userId) {
        requireLogin();
        if (userId == null || !userId.equals(getCurrentUserId())) {
            throw new BaseException("无权操作其他用户数据");
        }
    }

    public static void requireSelfOrAdmin(Long userId) {
        requireLogin();
        if (isAdminUser()) {
            return;
        }
        requireSelf(userId);
    }

    public static Long currentUserIdWhenBlank(Long userId) {
        requireLogin();
        return userId == null ? getCurrentUserId() : userId;
    }

    public static boolean hasText(String value) {
        return StringUtils.hasText(value);
    }
}
