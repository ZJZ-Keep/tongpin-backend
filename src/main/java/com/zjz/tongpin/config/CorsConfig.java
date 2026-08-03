package com.zjz.tongpin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")// 允许所有路径
                .allowedOrigins("http://localhost:3000") // 允许的域名
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")// 允许的请求方法
                .allowCredentials(true)       // 允许携带 cookie
                .maxAge(3600);// 预检请求的缓存时间
    }
}
