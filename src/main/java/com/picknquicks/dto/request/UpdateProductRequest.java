package com.picknquicks.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update product request")
public class UpdateProductRequest {

    @Schema(description = "Product name")
    @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
    private String name;

    @Schema(description = "URL-friendly slug")
    @Size(min = 3, max = 150, message = "Slug must be between 3 and 150 characters")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must be lowercase alphanumeric with hyphens")
    private String slug;

    @Schema(description = "Stock Keeping Unit")
    @Size(min = 2, max = 100, message = "SKU must be between 2 and 100 characters")
    private String sku;

    @Schema(description = "Product description")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Schema(description = "Short description")
    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDescription;

    @Schema(description = "Product price")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    @Schema(description = "Sale price")
    @DecimalMin(value = "0.01", message = "Sale price must be greater than 0")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal salePrice;

    @Schema(description = "Cost price")
    @DecimalMin(value = "0.01", message = "Cost price must be greater than 0")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal costPrice;

    @Schema(description = "Tax rate percentage")
    @DecimalMin(value = "0.00", message = "Tax rate cannot be negative")
    @DecimalMax(value = "100.00", message = "Tax rate cannot exceed 100%")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal taxRate;

    @Schema(description = "Category ID")
    private UUID categoryId;

    @Schema(description = "Brand ID")
    private UUID brandId;

    @Schema(description = "Low stock threshold")
    @Min(value = 1, message = "Low stock threshold must be at least 1")
    private Integer lowStockThreshold;

    @Schema(description = "Weight in grams")
    @Min(value = 0, message = "Weight cannot be negative")
    private Integer weightGrams;

    @Schema(description = "Dimensions")
    @Size(max = 50, message = "Dimensions must not exceed 50 characters")
    private String dimensions;

    @Schema(description = "Is product active")
    private Boolean active;

    @Schema(description = "Is product featured")
    private Boolean featured;

    @Schema(description = "Is digital product")
    private Boolean isDigital;

    @Schema(description = "Requires shipping")
    private Boolean requiresShipping;

    @Schema(description = "Display order")
    private Integer displayOrder;

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