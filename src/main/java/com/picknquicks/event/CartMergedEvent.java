package com.picknquicks.event;
import java.util.UUID;

public class CartMergedEvent extends CartEvent {

    private final UUID guestCartId;
    private final UUID userCartId;

    public CartMergedEvent(Object source, UUID guestCartId, UUID userCartId) {
        super(source, userCartId, "CART_MERGED");
        this.guestCartId = guestCartId;
        this.userCartId = userCartId;
    }

    public UUID getGuestCartId() {
        return guestCartId;
    }

    public UUID getUserCartId() {
        return userCartId;
    }
}