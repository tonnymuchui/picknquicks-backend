package com.picknquicks.mapper;
import com.picknquicks.domain.cart.Cart;
import com.picknquicks.domain.cart.CartItem;
import com.picknquicks.dto.response.cart.CartItemResponse;
import com.picknquicks.dto.response.cart.CartResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartResponse toCartResponse(Cart cart) {
        if (cart == null) {
            return null;
        }

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser() != null ? cart.getUser().getId() : null)
                .guestToken(cart.getGuestToken())
                .status(cart.getStatus().name())
                .items(mapItems(cart.getItems()))
                .totalItems(cart.getTotalItems())
                .subtotal(cart.getSubtotal())
                .tax(cart.getTax())
                .total(cart.getTotal())
                .isGuest(cart.isGuest())
                .expiresAt(cart.getExpiresAt())
                .lastActivityAt(cart.getLastActivityAt())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private List<CartItemResponse> mapItems(Set<CartItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        return items.stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList());
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        var product = item.getProduct();
        var currentPrice = product.getEffectivePrice();
        var priceChanged = !item.getPrice().equals(currentPrice);

        return CartItemResponse.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productSku(product.getSku())
                .productSlug(product.getSlug())
                .productImageUrl(product.getPrimaryImage() != null ? product.getPrimaryImage().getImageUrl() : null)
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .taxRate(item.getTaxRate())
                .itemTotal(item.getItemTotal())
                .taxAmount(item.getTaxAmount())
                .totalWithTax(item.getTotalWithTax())
                .inStock(product.isInStock())
                .availableStock(product.getStockQuantity())
                .priceChanged(priceChanged)
                .currentPrice(priceChanged ? currentPrice : null)
                .build();
    }
}