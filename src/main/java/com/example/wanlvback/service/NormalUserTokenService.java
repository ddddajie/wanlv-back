package com.example.wanlvback.service;

import com.example.wanlvback.config.JwtProperties;
import com.example.wanlvback.exception.UnauthorizedException;
import com.example.wanlvback.mapper.SysNormalUserMapper;
import com.example.wanlvback.mapper.SysNormalUserRefreshTokenMapper;
import com.example.wanlvback.pojo.entity.SysNormalUser;
import com.example.wanlvback.pojo.entity.SysNormalUserRefreshToken;
import com.example.wanlvback.pojo.vo.TokenRefreshVO;
import com.example.wanlvback.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

/**
 * 普通用户 Token 签发与轮换服务。
 */
@Service
public class NormalUserTokenService {

    private static final int REFRESH_TOKEN_BYTES = 32;
    private static final int MAX_REFRESH_TOKEN_LENGTH = 512;
    private static final String LOGIN_EXPIRED_MESSAGE = "登录状态已过期，请重新登录";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private SysNormalUserMapper sysNormalUserMapper;

    @Autowired
    private SysNormalUserRefreshTokenMapper refreshTokenMapper;

    /**
     * 每次登录都新建一条记录，因此不同设备拥有相互独立的 refreshToken。
     */
    public TokenRefreshVO issueTokens(SysNormalUser user) {
        LocalDateTime now = LocalDateTime.now();
        String refreshToken = createRefreshToken();
        refreshTokenMapper.insert(SysNormalUserRefreshToken.builder()
                .tokenHash(hashRefreshToken(refreshToken))
                .userId(user.getId())
                .expireTime(now.plusSeconds(getRefreshExpireSeconds()))
                .invalidated(0)
                .build());

        return TokenRefreshVO.builder()
                .token(createAccessToken(user))
                .refreshToken(refreshToken)
                .expireSeconds(getAccessExpireSeconds())
                .refreshExpireSeconds(getRefreshExpireSeconds())
                .build();
    }

    /**
     * 重点：先以条件更新原子作废旧令牌，防止并发请求重复刷新成功。
     */
    @Transactional(rollbackFor = Exception.class)
    public TokenRefreshVO refresh(String refreshToken) {
        String tokenHash = resolveTokenHash(refreshToken);
        LocalDateTime now = LocalDateTime.now();
        SysNormalUserRefreshToken storedToken = refreshTokenMapper.getByTokenHash(tokenHash);
        if (storedToken == null
                || Integer.valueOf(1).equals(storedToken.getInvalidated())
                || storedToken.getExpireTime() == null
                || !storedToken.getExpireTime().isAfter(now)) {
            throw unauthorized();
        }

        SysNormalUser user = sysNormalUserMapper.getById(storedToken.getUserId());
        if (user == null
                || Integer.valueOf(1).equals(user.getDeleted())
                || Integer.valueOf(0).equals(user.getStatus())) {
            throw unauthorized();
        }

        if (refreshTokenMapper.invalidateIfAvailable(tokenHash, now) != 1) {
            throw unauthorized();
        }
        return issueTokens(user);
    }

    /**
     * 退出接口保持幂等：令牌不存在或已失效时也无需向客户端泄露状态。
     */
    @Transactional(rollbackFor = Exception.class)
    public void logout(String refreshToken) {
        if (!StringUtils.hasText(refreshToken) || refreshToken.length() > MAX_REFRESH_TOKEN_LENGTH) {
            return;
        }
        refreshTokenMapper.invalidate(hashRefreshToken(refreshToken.trim()));
    }

    private String createAccessToken(SysNormalUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("userType", "normal");
        claims.put("role", "normal_user");
        return JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTtlMs(), claims);
    }

    private String createRefreshToken() {
        byte[] randomBytes = new byte[REFRESH_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String resolveTokenHash(String refreshToken) {
        if (!StringUtils.hasText(refreshToken) || refreshToken.length() > MAX_REFRESH_TOKEN_LENGTH) {
            throw unauthorized();
        }
        return hashRefreshToken(refreshToken.trim());
    }

    private String hashRefreshToken(String refreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("当前运行环境不支持 SHA-256", ex);
        }
    }

    private Long getAccessExpireSeconds() {
        return jwtProperties.getTtlMs() / 1000L;
    }

    private Long getRefreshExpireSeconds() {
        return jwtProperties.getRefreshTtlMs() / 1000L;
    }

    private UnauthorizedException unauthorized() {
        return new UnauthorizedException(LOGIN_EXPIRED_MESSAGE);
    }
}
