package com.picknquicks.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create category request")
public class CreateCategoryRequest {

    @Schema(description = "Category name", example = "Electronics")
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 128, message = "Name must be between 2 and 128 characters")
    private String name;

    @Schema(description = "URL-friendly slug", example = "electronics")
    @NotBlank(message = "Slug is required")
    @Size(min = 2, max = 150, message = "Slug must be between 2 and 150 characters")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must be lowercase alphanumeric with hyphens")
    private String slug;

    @Schema(description = "Category description")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Schema(description = "Category image URL")
    @Size(max = 255, message = "Image URL must not exceed 255 characters")
    private String imageUrl;

    @Schema(description = "Category image file", type = "string", format = "binary")
    private MultipartFile imageFile;

    @Schema(description = "Category icon URL")
    @Size(max = 255, message = "Icon URL must not exceed 255 characters")
    private String iconUrl;

    @Schema(description = "Category icon file", type = "string", format = "binary")
    private MultipartFile iconFile;

    @Schema(description = "Parent category ID (null for root category)")
    private UUID parentId;

    @Schema(description = "Display order", example = "0")
    private Integer displayOrder;

    @Schema(description = "Is category active", example = "true")
    @Builder.Default
    private Boolean active = true;

    @Schema(description = "Meta title for SEO")
    @Size(max = 128, message = "Meta title must not exceed 128 characters")
    private String metaTitle;

    @Schema(description = "Meta description for SEO")
    @Size(max = 255, message = "Meta description must not exceed 255 characters")
    private String metaDescription;

    @Schema(description = "Meta keywords for SEO")
    @Size(max = 255, message = "Meta keywords must not exceed 255 characters")
    private String metaKeywords;
}