package com.picknquicks.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.file-storage")
@Data
public class FileStorageConfig {
    private String basePath;
    private String avatarPath = "avatars";
    private String productPath = "products";
    private String categoryPath = "categories";
    private Long maxFileSize = 10485760L;
    private String[] allowedMimeTypes = {
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    };

    @PostConstruct
    public void init() {
        if (basePath == null || basePath.isBlank()) {
            basePath = System.getProperty("user.dir") + "/uploads";
        }
    }

    public String getAvatarStoragePath() {
        return basePath + "/" + avatarPath;
    }

    public String getProductStoragePath() {
        return basePath + "/" + productPath;
    }

    public String getCategoryStoragePath() {
        return basePath + "/" + categoryPath;
    }
}





