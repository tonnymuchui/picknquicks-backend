package com.picknquicks.dto.request.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Shipping address request")
public class ShippingAddressRequest {

    @Schema(description = "Recipient name", example = "John Doe")
    @NotBlank(message = "Recipient name is required")
    private String recipientName;

    @Schema(description = "Phone number", example = "+254712345678")
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @Schema(description = "Address line 1", example = "123 Main Street")
    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;

    @Schema(description = "Address line 2", example = "Apartment 4B")
    private String addressLine2;

    @Schema(description = "City", example = "Nairobi")
    @NotBlank(message = "City is required")
    private String city;

    @Schema(description = "County", example = "Nairobi County")
    private String county;

    @Schema(description = "Postal code", example = "00100")
    private String postalCode;

    @Schema(description = "Delivery notes")
    private String notes;
}