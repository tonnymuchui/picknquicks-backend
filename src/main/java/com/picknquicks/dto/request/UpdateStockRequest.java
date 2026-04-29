package com.picknquicks.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update stock request")
public class UpdateStockRequest {

    @Schema(description = "Quantity to add (positive) or remove (negative)", example = "10")
    @NotNull(message = "Quantity is required")
    private Integer quantity;

    @Schema(description = "Reason for stock change", example = "Received new shipment")
    private String reason;
}