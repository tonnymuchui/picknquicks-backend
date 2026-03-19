package com.picknquicks.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    
    String storeFile(MultipartFile file, String entityType) throws IOException;

    String storeFile(MultipartFile file, String entityType, String filename) throws IOException;

    void deleteFile(String filePath);

    String getAbsoluteFilePath(String relativeFilePath);

    boolean fileExists(String filePath);

    void validateFile(MultipartFile file);
}

