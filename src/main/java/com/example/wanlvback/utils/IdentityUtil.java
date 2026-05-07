package com.example.wanlvback.utils;

import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class IdentityUtil {

    private static final int[] ID_CARD_WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final char[] ID_CARD_CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    private IdentityUtil() {
    }

    public static String normalizeIdCardNo(String idCardNo) {
        return StringUtils.hasText(idCardNo) ? idCardNo.trim().toUpperCase() : null;
    }

    public static boolean isValidIdCardNo(String idCardNo) {
        String normalized = normalizeIdCardNo(idCardNo);
        if (normalized == null || !normalized.matches("^\\d{17}[0-9X]$")) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += Character.digit(normalized.charAt(i), 10) * ID_CARD_WEIGHTS[i];
        }
        return ID_CARD_CHECK_CODES[sum % 11] == normalized.charAt(17);
    }

    public static String maskIdCardNo(String idCardNo) {
        String normalized = normalizeIdCardNo(idCardNo);
        if (normalized == null || normalized.length() < 10) {
            return normalized;
        }
        return normalized.substring(0, 6) + "********" + normalized.substring(normalized.length() - 4);
    }

    public static String hashIdCardNo(String idCardNo) {
        String normalized = normalizeIdCardNo(idCardNo);
        if (normalized == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("身份证哈希生成失败", ex);
        }
    }
}
