package com.zjz.tongpin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web MVC 配置（静态资源映射）
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 头像本地存储目录
     */
    @Value("${file.avatar.upload-dir:./upload/avatar}")
    private String avatarUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(avatarUploadDir).toAbsolutePath().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/avatar/**")
                .addResourceLocations(location);
    }
}
