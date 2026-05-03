package com.picknquicks.event;
import java.util.UUID;

public class CartItemAddedEvent extends CartEvent {

    private final UUID productId;
    private final Integer quantity;

    public CartItemAddedEvent(Object source, UUID cartId, UUID productId, Integer quantity) {
        super(source, cartId, "CART_ITEM_ADDED");
        this.productId = productId;
        this.quantity = quantity;
    }

    public UUID getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }
}