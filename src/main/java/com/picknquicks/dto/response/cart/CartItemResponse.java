package com.picknquicks.dto.response.cart;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Cart item response")
public class CartItemResponse {

    @Schema(description = "Cart item ID")
    private UUID id;

    @Schema(description = "Product ID")
    private UUID productId;

    @Schema(description = "Product name")
    private String productName;

    @Schema(description = "Product SKU")
    private String productSku;

    @Schema(description = "Product slug")
    private String productSlug;

    @Schema(description = "Product image URL")
    private String productImageUrl;

    @Schema(description = "Unit price")
    private BigDecimal price;

    @Schema(description = "Quantity")
    private Integer quantity;

    @Schema(description = "Tax rate")
    private BigDecimal taxRate;

    @Schema(description = "Item total (price * quantity)")
    private BigDecimal itemTotal;

    @Schema(description = "Tax amount")
    private BigDecimal taxAmount;

    @Schema(description = "Total with tax")
    private BigDecimal totalWithTax;

    @Schema(description = "Is product in stock")
    private Boolean inStock;

    @Schema(description = "Available stock quantity")
    private Integer availableStock;

    @Schema(description = "Price changed since added")
    private Boolean priceChanged;

    @Schema(description = "Current product price")
    private BigDecimal currentPrice;
}