package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 核心逻辑：虚拟路径映射真实本地路径
        registry.addResourceHandler("/picture/**")
                .addResourceLocations("file:D:/Hunkoufanchi/springboot/finalDegien/demo1/demo/picture/");
    }
}