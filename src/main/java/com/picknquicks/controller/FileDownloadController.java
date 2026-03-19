 package com.picknquicks.controller;

import com.picknquicks.service.storage.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Files", description = "File download endpoints")
public class FileDownloadController {

    private final FileStorageService fileStorageService;

    @GetMapping("/download/{type}/{filename}")
    @Operation(summary = "Download file", description = "Download a file by type and filename")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable String type,
            @PathVariable String filename
    ) {
        try {
            String filePath = type + "/" + filename;

            if (!fileStorageService.fileExists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            String absolutePath = fileStorageService.getAbsoluteFilePath(filePath);
            Path path = Paths.get(absolutePath);
            byte[] fileContent = Files.readAllBytes(path);

            String contentType = getContentType(filename);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .body(fileContent);
        } catch (IOException e) {
            log.error("Failed to download file: {}/{}", type, filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/preview/{type}/{filename}")
    @Operation(summary = "Preview file", description = "Preview a file in browser")
    public ResponseEntity<byte[]> previewFile(
            @PathVariable String type,
            @PathVariable String filename
    ) {
        try {
            String filePath = type + "/" + filename;

            if (!fileStorageService.fileExists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            String absolutePath = fileStorageService.getAbsoluteFilePath(filePath);
            Path path = Paths.get(absolutePath);
            byte[] fileContent = Files.readAllBytes(path);

            String contentType = getContentType(filename);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(fileContent);
        } catch (IOException e) {
            log.error("Failed to preview file: {}/{}", type, filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private String getContentType(String filename) {
        if (filename == null) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG_VALUE;
            case "png" -> MediaType.IMAGE_PNG_VALUE;
            case "gif" -> MediaType.IMAGE_GIF_VALUE;
            case "webp" -> "image/webp";
            case "pdf" -> MediaType.APPLICATION_PDF_VALUE;
            case "txt" -> MediaType.TEXT_PLAIN_VALUE;
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }
}

