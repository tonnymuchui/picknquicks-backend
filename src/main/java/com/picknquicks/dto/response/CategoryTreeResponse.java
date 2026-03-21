package com.picknquicks.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Category tree node")
public class CategoryTreeResponse {

    @Schema(description = "Category ID")
    private UUID id;

    @Schema(description = "Category name")
    private String name;

    @Schema(description = "URL-friendly slug")
    private String slug;

    @Schema(description = "Category icon URL")
    private String iconUrl;

    @Schema(description = "Is category active")
    private Boolean active;

    @Schema(description = "Display order")
    private Integer displayOrder;

    @Schema(description = "Category level")
    private Integer level;

    @Schema(description = "Child categories")
    private List<CategoryTreeResponse> children;
}