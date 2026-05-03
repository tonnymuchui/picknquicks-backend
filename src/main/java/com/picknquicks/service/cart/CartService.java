package com.picknquicks.service.cart;
import com.picknquicks.dto.request.cart.AddToCartRequest;
import com.picknquicks.dto.request.cart.UpdateCartItemRequest;
import com.picknquicks.dto.response.cart.CartResponse;
import java.util.UUID;

public interface CartService {

    CartResponse addToCart(AddToCartRequest request, String guestToken);

    CartResponse updateCartItem(UUID cartItemId, UpdateCartItemRequest request, String guestToken);

    CartResponse removeFromCart(UUID cartItemId, String guestToken);

    CartResponse clearCart(String guestToken);

    CartResponse getCart(String guestToken);

    CartResponse mergeGuestCart(String guestToken, UUID userId);

    void markAsAbandoned(UUID cartId);

    void cleanupExpiredCarts();
}