package com.example.wanlvback.interceptor;

import com.example.wanlvback.config.JwtProperties;
import com.example.wanlvback.context.BaseContext;
import com.example.wanlvback.result.Result;
import com.example.wanlvback.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * JWT 登录态拦截器。
 */
@Component
@Slf4j
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            return rejectRequest(response, HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
        }

        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
            Number userIdClaim = claims.get("userId", Number.class);
            Long userId = userIdClaim == null ? null : userIdClaim.longValue();
            String userType = claims.get("userType", String.class);
            String role = claims.get("role", String.class);

            if (userId == null || !StringUtils.hasText(userType)) {
                return rejectRequest(response, HttpServletResponse.SC_UNAUTHORIZED, "登录状态无效");
            }

            // 重点：当前请求身份只从已校验的 JWT 中取，后续接口用 BaseContext 防越权。
            BaseContext.setCurrentId(userId);
            BaseContext.setCurrentUserType(userType);
            BaseContext.setCurrentRole(role);
            return true;
        } catch (Exception ex) {
            log.warn("JWT 校验失败，uri={}", request.getRequestURI(), ex);
            return rejectRequest(response, HttpServletResponse.SC_UNAUTHORIZED, "登录状态已失效，请重新登录");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }

    private String resolveToken(HttpServletRequest request) {
        String headerValue = request.getHeader(jwtProperties.getHeaderName());
        if (!StringUtils.hasText(headerValue)) {
            return null;
        }

        String tokenPrefix = jwtProperties.getTokenPrefix();
        if (StringUtils.hasText(tokenPrefix) && headerValue.startsWith(tokenPrefix + " ")) {
            return headerValue.substring(tokenPrefix.length() + 1).trim();
        }
        return headerValue.trim();
    }

    private boolean rejectRequest(HttpServletResponse response, int status, String message) throws Exception {
        Result<String> result = new Result<>();
        result.setCode(status);
        result.setMsg(message);

        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(result));
        return false;
    }
}
