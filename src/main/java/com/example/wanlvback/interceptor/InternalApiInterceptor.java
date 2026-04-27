package com.example.wanlvback.interceptor;

import com.example.wanlvback.config.InternalApiProperties;
import com.example.wanlvback.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * 内部接口拦截器
 */
@Component
@Slf4j
public class InternalApiInterceptor implements HandlerInterceptor {

    @Autowired
    private InternalApiProperties internalApiProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!internalApiProperties.isEnabled()) {
            return rejectRequest(response, HttpServletResponse.SC_FORBIDDEN, "内部接口未启用");
        }

        String remoteAddr = request.getRemoteAddr();
        if (!isSourceAllowed(remoteAddr)) {
            log.warn("拦截非内部来源请求: uri={}, remoteAddr={}", request.getRequestURI(), remoteAddr);
            return rejectRequest(response, HttpServletResponse.SC_FORBIDDEN, "仅允许内部网络访问");
        }

        String headerName = internalApiProperties.getHeaderName();
        String token = request.getHeader(headerName);
        if (!StringUtils.hasText(internalApiProperties.getToken()) || !internalApiProperties.getToken().equals(token)) {
            log.warn("拦截未授权内部请求: uri={}, remoteAddr={}", request.getRequestURI(), remoteAddr);
            return rejectRequest(response, HttpServletResponse.SC_UNAUTHORIZED, "内部接口认证失败");
        }

        return true;
    }

    private boolean isSourceAllowed(String remoteAddr) {
        if (!StringUtils.hasText(remoteAddr)) {
            return false;
        }

        if (!internalApiProperties.isAllowLoopback() && !internalApiProperties.isAllowPrivateNetwork()) {
            return true;
        }

        try {
            InetAddress address = InetAddress.getByName(remoteAddr);

            if (internalApiProperties.isAllowLoopback()
                    && (address.isLoopbackAddress() || address.isAnyLocalAddress())) {
                return true;
            }

            if (internalApiProperties.isAllowPrivateNetwork()
                    && (address.isSiteLocalAddress() || address.isLinkLocalAddress() || isUniqueLocalIpv6(remoteAddr))) {
                return true;
            }
        } catch (Exception ex) {
            log.warn("解析请求来源失败: remoteAddr={}", remoteAddr, ex);
        }

        return false;
    }

    private boolean isUniqueLocalIpv6(String remoteAddr) {
        String normalized = remoteAddr == null ? "" : remoteAddr.toLowerCase();
        return normalized.startsWith("fc") || normalized.startsWith("fd");
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
