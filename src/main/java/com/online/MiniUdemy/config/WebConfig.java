package com.online.MiniUdemy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Find the exact physical location of the uploads folder on your computer
        String uploadPath = Paths.get("src/main/resources/static/uploads").toFile().getAbsolutePath();
        
        // Tell Spring Boot to serve files directly from that physical folder
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}