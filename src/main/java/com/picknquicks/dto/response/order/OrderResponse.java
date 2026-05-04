package com.picknquicks.dto.response.order;

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
@Schema(description = "Order response")
public class OrderResponse {

    @Schema(description = "Order ID")
    private UUID id;

    @Schema(description = "Order number")
    private String orderNumber;

    @Schema(description = "User ID (null for guest)")
    private UUID userId;

    @Schema(description = "Customer email")
    private String email;

    @Schema(description = "Customer phone number")
    private String phoneNumber;

    @Schema(description = "Customer name")
    private String customerName;

    @Schema(description = "Order status")
    private String status;

    @Schema(description = "Payment method")
    private String paymentMethod;

    @Schema(description = "Payment status")
    private String paymentStatus;

    @Schema(description = "Order items")
    private List<OrderItemResponse> items;

    @Schema(description = "Shipping address")
    private OrderAddressResponse shippingAddress;

    @Schema(description = "Payment details")
    private PaymentResponse payment;

    @Schema(description = "Subtotal amount")
    private BigDecimal subtotal;

    @Schema(description = "Tax amount")
    private BigDecimal taxAmount;

    @Schema(description = "Shipping cost")
    private BigDecimal shippingCost;

    @Schema(description = "Total amount")
    private BigDecimal totalAmount;

    @Schema(description = "Amount to pay (shipping only for COD)")
    private BigDecimal amountToPay;

    @Schema(description = "Order notes")
    private String notes;

    @Schema(description = "Tracking number")
    private String trackingNumber;

    @Schema(description = "Estimated delivery date")
    private LocalDateTime estimatedDeliveryDate;

    @Schema(description = "Is guest order")
    private Boolean isGuest;

    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Updated timestamp")
    private LocalDateTime updatedAt;
}