package com.picknquicks.dto.request.order;

import com.picknquicks.domain.order.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create order request")
public class CreateOrderRequest {

    @Schema(description = "Customer email", example = "customer@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Customer phone number", example = "+254712345678")
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+254[17]\\d{8}$", message = "Invalid Kenyan phone number format")
    private String phoneNumber;

    @Schema(description = "Customer name", example = "John Doe")
    @NotBlank(message = "Customer name is required")
    private String customerName;

    @Schema(description = "Payment method")
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Schema(description = "Shipping address")
    @NotNull(message = "Shipping address is required")
    @Valid
    private ShippingAddressRequest shippingAddress;

    @Schema(description = "Order notes")
    private String notes;
}