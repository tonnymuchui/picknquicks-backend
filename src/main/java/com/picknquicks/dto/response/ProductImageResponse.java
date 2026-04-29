package com.picknquicks.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Product image response")
public class ProductImageResponse {

    @Schema(description = "Image ID")
    private UUID id;

    @Schema(description = "Image URL")
    private String imageUrl;

    @Schema(description = "Alt text")
    private String altText;

    @Schema(description = "Is primary image")
    private Boolean isPrimary;

    @Schema(description = "Display order")
    private Integer displayOrder;
}