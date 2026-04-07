package com.picknquicks.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Category response")
public class CategoryResponse {

    @Schema(description = "Category ID")
    private UUID id;

    @Schema(description = "Category name")
    private String name;

    @Schema(description = "URL-friendly slug")
    private String slug;

    @Schema(description = "Category description")
    private String description;

    @Schema(description = "Category image URL")
    private String imageUrl;

    @Schema(description = "Category icon URL")
    private String iconUrl;

    @Schema(description = "Is category active")
    private Boolean active;

    @Schema(description = "Display order")
    private Integer displayOrder;

    @Schema(description = "Parent category ID")
    private UUID parentId;

    @Schema(description = "Parent category name")
    private String parentName;

    @Schema(description = "Category level/depth in hierarchy")
    private Integer level;

    @Schema(description = "Full category path")
    private String fullPath;

    @Schema(description = "Has children")
    private Boolean hasChildren;

    @Schema(description = "Number of children")
    private Integer childrenCount;

    @Schema(description = "Child categories")
    private Set<CategoryResponse> children;

    @Schema(description = "Meta title for SEO")
    private String metaTitle;

    @Schema(description = "Meta description for SEO")
    private String metaDescription;

    @Schema(description = "Meta keywords for SEO")
    private String metaKeywords;

    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last updated timestamp")
    private LocalDateTime updatedAt;
}
