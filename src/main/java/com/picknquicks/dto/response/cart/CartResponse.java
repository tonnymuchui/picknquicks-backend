package com.picknquicks.dto.response.cart;
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
@Schema(description = "Cart response")
public class CartResponse {

    @Schema(description = "Cart ID")
    private UUID id;

    @Schema(description = "User ID (null for guest)")
    private UUID userId;

    @Schema(description = "Guest token (null for authenticated)")
    private String guestToken;

    @Schema(description = "Cart status")
    private String status;

    @Schema(description = "Cart items")
    private List<CartItemResponse> items;

    @Schema(description = "Total number of items")
    private Integer totalItems;

    @Schema(description = "Subtotal amount")
    private BigDecimal subtotal;

    @Schema(description = "Tax amount")
    private BigDecimal tax;

    @Schema(description = "Total amount")
    private BigDecimal total;

    @Schema(description = "Is guest cart")
    private Boolean isGuest;

    @Schema(description = "Expires at (for guest carts)")
    private LocalDateTime expiresAt;

    @Schema(description = "Last activity timestamp")
    private LocalDateTime lastActivityAt;

    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Updated timestamp")
    private LocalDateTime updatedAt;
}