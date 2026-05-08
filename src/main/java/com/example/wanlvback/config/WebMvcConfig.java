package com.example.wanlvback.config;

import com.example.wanlvback.interceptor.InternalApiInterceptor;
import com.example.wanlvback.interceptor.JwtAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC 配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private InternalApiInterceptor internalApiInterceptor;

    @Autowired
    private JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(internalApiInterceptor)
                .addPathPatterns("/internal/**");

        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/internal/**",
                        "/user/init",
                        "/user/admin/login",
                        "/user/normal/login",
                        "/user/normal/register",
                        "/user/normal/code/send",
                        "/user/normal/code/login",
                        "/reservation/agent/**",
                        "/map/agent/**",
                        "/reservation/spots/enabled",
                        "/reservation/slots",
                        "/map/init/{scenicAreaId}",
                        "/map/scenic-areas/page"
                );
    }
}
