package com.tfgkk.schoolcompetition.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 保存后重启后端，这将覆盖所有控制器的跨域设置
        registry.addMapping("/**") // 对所有接口生效
                .allowedOriginPatterns("*") // 允许所有来源（开发环境建议）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的请求方式
                .allowedHeaders("*") // 允许的 Header
                .allowCredentials(true) // 是否允许携带 Cookie
                .maxAge(3600); // 预检请求的有效期（秒）
    }
}