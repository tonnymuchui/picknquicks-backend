package com.picknquicks.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create product request")
public class CreateProductRequest {

    @Schema(description = "Product name", example = "LG 34-inch UltraWide Monitor")
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
    private String name;

    @Schema(description = "URL-friendly slug", example = "lg-34-ultrawide-monitor")
    @NotBlank(message = "Slug is required")
    @Size(min = 3, max = 150, message = "Slug must be between 3 and 150 characters")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must be lowercase alphanumeric with hyphens")
    private String slug;

    @Schema(description = "Stock Keeping Unit", example = "LG-34WN80C-B")
    @NotBlank(message = "SKU is required")
    @Size(min = 2, max = 100, message = "SKU must be between 2 and 100 characters")
    private String sku;

    @Schema(description = "Product description")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Schema(description = "Short description")
    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDescription;

    @Schema(description = "Product price", example = "599.99")
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;

    @Schema(description = "Sale price", example = "499.99")
    @DecimalMin(value = "0.01", message = "Sale price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Sale price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal salePrice;

    @Schema(description = "Cost price", example = "350.00")
    @DecimalMin(value = "0.01", message = "Cost price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Cost price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal costPrice;

    @Schema(description = "Tax rate percentage", example = "16.00")
    @DecimalMin(value = "0.00", message = "Tax rate cannot be negative")
    @DecimalMax(value = "100.00", message = "Tax rate cannot exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Tax rate must have at most 3 integer digits and 2 decimal places")
    private BigDecimal taxRate;

    @Schema(description = "Category ID")
    @NotNull(message = "Category is required")
    private UUID categoryId;

    @Schema(description = "Brand ID")
    private UUID brandId;

    @Schema(description = "Stock quantity", example = "50")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Schema(description = "Low stock threshold", example = "10")
    @Min(value = 1, message = "Low stock threshold must be at least 1")
    private Integer lowStockThreshold;

    @Schema(description = "Weight in grams", example = "8500")
    @Min(value = 0, message = "Weight cannot be negative")
    private Integer weightGrams;

    @Schema(description = "Dimensions (LxWxH)", example = "81.5 x 36.3 x 46.8 cm")
    @Size(max = 50, message = "Dimensions must not exceed 50 characters")
    private String dimensions;

    @Schema(description = "Is product active", example = "true")
    @Builder.Default
    private Boolean active = true;

    @Schema(description = "Is product featured", example = "false")
    @Builder.Default
    private Boolean featured = false;

    @Schema(description = "Is digital product", example = "false")
    @Builder.Default
    private Boolean isDigital = false;

    @Schema(description = "Requires shipping", example = "true")
    @Builder.Default
    private Boolean requiresShipping = true;

    @Schema(description = "Display order", example = "0")
    private Integer displayOrder;

    @Schema(description = "Product images")
    private List<MultipartFile> imageFiles;

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