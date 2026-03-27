package com.picknquicks.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update brand request")
public class UpdateBrandRequest {

    @Schema(description = "Brand name", example = "Apple")
    @Size(min = 2, max = 128, message = "Name must be between 2 and 128 characters")
    private String name;

    @Schema(description = "URL-friendly slug", example = "apple")
    @Size(min = 2, max = 150, message = "Slug must be between 2 and 150 characters")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must be lowercase alphanumeric with hyphens")
    private String slug;

    @Schema(description = "Brand description")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Schema(description = "Brand logo URL (if not uploading file)")
    @Size(max = 255, message = "Logo URL must not exceed 255 characters")
    private String logoUrl;

    @Schema(description = "Brand banner URL (if not uploading file)")
    @Size(max = 255, message = "Banner URL must not exceed 255 characters")
    private String bannerUrl;

    @Schema(description = "Brand logo file")
    private MultipartFile logoFile;

    @Schema(description = "Brand banner file")
    private MultipartFile bannerFile;

    @Schema(description = "Official website URL")
    @Size(max = 255, message = "Website URL must not exceed 255 characters")
    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$",
            message = "Invalid website URL format")
    private String websiteUrl;

    @Schema(description = "Country of origin", example = "USA")
    @Size(max = 64, message = "Country must not exceed 64 characters")
    private String countryOfOrigin;

    @Schema(description = "Display order", example = "0")
    private Integer displayOrder;

    @Schema(description = "Is brand active", example = "true")
    private Boolean active;

    @Schema(description = "Is brand featured", example = "false")
    private Boolean featured;

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