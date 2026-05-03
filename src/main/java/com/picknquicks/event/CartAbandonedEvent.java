package com.picknquicks.event;
import java.util.UUID;

public class CartAbandonedEvent extends CartEvent {

    private final String email;
    private final Integer itemCount;

    public CartAbandonedEvent(Object source, UUID cartId, String email, Integer itemCount) {
        super(source, cartId, "CART_ABANDONED");
        this.email = email;
        this.itemCount = itemCount;
    }

    public String getEmail() {
        return email;
    }

    public Integer getItemCount() {
        return itemCount;
    }
}