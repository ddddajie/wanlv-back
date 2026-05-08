package com.example.wanlvback.utils;

import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;

public final class IdentityUtil {

    private static final int[] ID_CARD_WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final char[] ID_CARD_CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
    private static final DateTimeFormatter ID_CARD_BIRTHDAY_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

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

    public static Integer resolveGender(String idCardNo) {
        String normalized = normalizeIdCardNo(idCardNo);
        if (normalized == null || normalized.length() < 17) {
            return null;
        }
        int genderCode = Character.digit(normalized.charAt(16), 10);
        if (genderCode < 0) {
            return null;
        }
        // 身份证第17位为性别码，奇数为男，偶数为女。
        return genderCode % 2 == 1 ? 1 : 2;
    }

    public static LocalDate resolveBirthday(String idCardNo) {
        String normalized = normalizeIdCardNo(idCardNo);
        if (normalized == null || normalized.length() < 14) {
            return null;
        }
        try {
            // 身份证第7-14位为出生日期，格式为yyyyMMdd。
            return LocalDate.parse(normalized.substring(6, 14), ID_CARD_BIRTHDAY_FORMATTER);
        } catch (DateTimeException ex) {
            return null;
        }
    }

    public static Integer resolveAge(String idCardNo) {
        LocalDate birthday = resolveBirthday(idCardNo);
        if (birthday == null || birthday.isAfter(LocalDate.now())) {
            return null;
        }
        return Period.between(birthday, LocalDate.now()).getYears();
    }
}
