package com.picknquicks.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Product response")
public class ProductResponse {

    @Schema(description = "Product ID")
    private UUID id;

    @Schema(description = "Product name")
    private String name;

    @Schema(description = "URL-friendly slug")
    private String slug;

    @Schema(description = "Stock Keeping Unit")
    private String sku;

    @Schema(description = "Product description")
    private String description;

    @Schema(description = "Short description")
    private String shortDescription;

    @Schema(description = "Product price")
    private BigDecimal price;

    @Schema(description = "Sale price")
    private BigDecimal salePrice;

    @Schema(description = "Effective price (sale price if on sale, else regular price)")
    private BigDecimal effectivePrice;

    @Schema(description = "Discount percentage")
    private BigDecimal discountPercentage;

    @Schema(description = "Category ID")
    private UUID categoryId;

    @Schema(description = "Category name")
    private String categoryName;

    @Schema(description = "Brand ID")
    private UUID brandId;

    @Schema(description = "Brand name")
    private String brandName;

    @Schema(description = "Stock quantity")
    private Integer stockQuantity;

    @Schema(description = "Is in stock")
    private Boolean inStock;

    @Schema(description = "Is low stock")
    private Boolean lowStock;

    @Schema(description = "Weight in grams")
    private Integer weightGrams;

    @Schema(description = "Dimensions")
    private String dimensions;

    @Schema(description = "Is product active")
    private Boolean active;

    @Schema(description = "Is product featured")
    private Boolean featured;

    @Schema(description = "Is digital product")
    private Boolean isDigital;

    @Schema(description = "Requires shipping")
    private Boolean requiresShipping;

    @Schema(description = "Average rating")
    private BigDecimal averageRating;

    @Schema(description = "Number of reviews")
    private Long reviewCount;

    @Schema(description = "Number of sales")
    private Long saleCount;

    @Schema(description = "Number of views")
    private Long viewCount;

    @Schema(description = "Primary image URL")
    private String primaryImageUrl;

    @Schema(description = "Product images")
    private List<ProductImageResponse> images;

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