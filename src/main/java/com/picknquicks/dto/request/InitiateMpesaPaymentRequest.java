package com.picknquicks.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for initiating M-Pesa STK Push payment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateMpesaPaymentRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^(254|\\+254|0)?[17]\\d{8}$",
        message = "Phone number must be a valid Kenyan number (format: 254XXXXXXXXX, 0XXXXXXXXX, or +254XXXXXXXXX)"
    )
    private String phoneNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1", message = "Amount must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Amount must not exceed 999999.99")
    private BigDecimal amount;
}

