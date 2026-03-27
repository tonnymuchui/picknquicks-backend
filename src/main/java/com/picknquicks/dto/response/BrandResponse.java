package com.picknquicks.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Brand response")
public class BrandResponse {

    @Schema(description = "Brand ID")
    private UUID id;

    @Schema(description = "Brand name")
    private String name;

    @Schema(description = "URL-friendly slug")
    private String slug;

    @Schema(description = "Brand description")
    private String description;

    @Schema(description = "Brand logo URL")
    private String logoUrl;

    @Schema(description = "Brand banner URL")
    private String bannerUrl;

    @Schema(description = "Official website URL")
    private String websiteUrl;

    @Schema(description = "Country of origin")
    private String countryOfOrigin;

    @Schema(description = "Is brand active")
    private Boolean active;

    @Schema(description = "Is brand featured")
    private Boolean featured;

    @Schema(description = "Display order")
    private Integer displayOrder;

    @Schema(description = "Number of products")
    private Long productCount;

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