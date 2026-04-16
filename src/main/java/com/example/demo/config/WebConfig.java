package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 核心逻辑：虚拟路径映射真实本地路径
        // 当访问网络路径 /picture/** 时，Spring Boot 会自动去 D 盘对应的文件夹下找文件
        // 注意：Windows 路径建议用正斜杠 /，且最后一定要加 /
        registry.addResourceHandler("/picture/**")
                .addResourceLocations("file:D:/Hunkoufanchi/springboot/finalDegien/demo1/demo/picture/");
    }
}