package com.picknquicks.service.storage;
import com.picknquicks.config.FileStorageConfig;
import com.picknquicks.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private final FileStorageConfig fileStorageConfig;

    @Override
    public String storeFile(MultipartFile file, String entityType) throws IOException {
        validateFile(file);
        String filename = generateUniqueFilename(file.getOriginalFilename());
        return storeFile(file, entityType, filename);
    }

    @Override
    public String storeFile(MultipartFile file, String entityType, String filename) throws IOException {
        validateFile(file);

        String entityFolder = resolveEntityFolder(entityType);
        Path uploadDir = Paths.get(fileStorageConfig.getBasePath(), entityFolder).toAbsolutePath();

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path filePath = uploadDir.resolve(filename);

        try {
            Files.write(filePath, file.getBytes());
            String relativeFilePath = entityFolder + "/" + filename;
            log.info("File stored: {} ({} bytes)", relativeFilePath, file.getSize());
            return relativeFilePath;
        } catch (IOException e) {
            log.error("Failed to store file: {}", filename, e);
            throw new BadRequestException("Failed to store file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }

        for (String candidate : buildPathCandidates(filePath)) {
            try {
                Path fileToDelete = Paths.get(fileStorageConfig.getBasePath(), candidate).toAbsolutePath().normalize();
                if (Files.exists(fileToDelete)) {
                    Files.delete(fileToDelete);
                    log.info("Deleted file: {}", candidate);
                    return;
                }
            } catch (IOException e) {
                log.warn("Could not delete file: {}", candidate, e);
            }
        }
    }

    @Override
    public String getAbsoluteFilePath(String relativeFilePath) {
        String resolved = resolveBestRelativePath(relativeFilePath);
        return Paths.get(fileStorageConfig.getBasePath(), resolved)
                .toAbsolutePath()
                .toString();
    }

    @Override
    public boolean fileExists(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return false;
        }

        for (String candidate : buildPathCandidates(filePath)) {
            if (Files.exists(Paths.get(fileStorageConfig.getBasePath(), candidate).toAbsolutePath().normalize())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > fileStorageConfig.getMaxFileSize()) {
            throw new BadRequestException("File size exceeds maximum allowed size of " + 
                    (fileStorageConfig.getMaxFileSize() / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedMimeType(contentType)) {
            throw new BadRequestException("File type not allowed. Allowed types: " + 
                    String.join(", ", fileStorageConfig.getAllowedMimeTypes()));
        }
    }

    private String generateUniqueFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "file";
        }

        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString() + extension;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private boolean isAllowedMimeType(String contentType) {
        return Arrays.asList(fileStorageConfig.getAllowedMimeTypes()).contains(contentType);
    }

    private String resolveEntityFolder(String entityType) {
        return switch (entityType.toLowerCase()) {
            case "avatar", "avatars" -> fileStorageConfig.getAvatarPath();
            case "product", "products" -> fileStorageConfig.getProductPath();
            case "category", "categories" -> fileStorageConfig.getCategoryPath();
            default -> entityType;
        };
    }

    private String normalizeRelativePath(String rawPath) {
        String normalized = rawPath.replace("\\", "/").trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private List<String> buildPathCandidates(String rawPath) {
        String normalized = normalizeRelativePath(rawPath);
        List<String> candidates = new ArrayList<>();
        candidates.add(normalized);

        if (normalized.startsWith("avatar/")) {
            candidates.add(fileStorageConfig.getAvatarPath() + "/" + normalized.substring("avatar/".length()));
        } else if (normalized.startsWith("avatars/")) {
            candidates.add("avatar/" + normalized.substring("avatars/".length()));
        }

        if (normalized.startsWith("product/")) {
            candidates.add(fileStorageConfig.getProductPath() + "/" + normalized.substring("product/".length()));
        } else if (normalized.startsWith("products/")) {
            candidates.add("product/" + normalized.substring("products/".length()));
        }

        if (normalized.startsWith("category/")) {
            candidates.add(fileStorageConfig.getCategoryPath() + "/" + normalized.substring("category/".length()));
        } else if (normalized.startsWith("categories/")) {
            candidates.add("category/" + normalized.substring("categories/".length()));
        }

        return candidates.stream().distinct().toList();
    }

    private String resolveBestRelativePath(String rawPath) {
        for (String candidate : buildPathCandidates(rawPath)) {
            if (Files.exists(Paths.get(fileStorageConfig.getBasePath(), candidate).toAbsolutePath().normalize())) {
                return candidate;
            }
        }
        return normalizeRelativePath(rawPath);
    }
}

