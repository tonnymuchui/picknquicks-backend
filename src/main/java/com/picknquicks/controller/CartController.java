package com.picknquicks.controller;

import com.picknquicks.aspect.RateLimited;
import com.picknquicks.dto.request.cart.AddToCartRequest;
import com.picknquicks.dto.request.cart.UpdateCartItemRequest;
import com.picknquicks.dto.response.ApiResponse;
import com.picknquicks.dto.response.cart.CartResponse;
import com.picknquicks.service.cart.CartService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart endpoints")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get current cart")
    @Timed(value = "cart.get", description = "Time taken to get cart")
    public ResponseEntity<ApiResponse> getCart(
            @RequestHeader(value = "X-Guest-Token", required = false) String headerGuestToken,
            @RequestParam(value = "guestToken", required = false) String paramGuestToken
    ) {
        String guestToken = headerGuestToken != null ? headerGuestToken : paramGuestToken;
        CartResponse response = cartService.getCart(guestToken);
        return ResponseEntity.ok(ApiResponse.success("Cart fetched successfully", response));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    @RateLimited
    @Timed(value = "cart.add", description = "Time taken to add to cart")
    public ResponseEntity<ApiResponse> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            @RequestHeader(value = "X-Guest-Token", required = false) String headerGuestToken,
            @RequestParam(value = "guestToken", required = false) String paramGuestToken
    ) {
        String guestToken = headerGuestToken != null ? headerGuestToken : paramGuestToken;
        CartResponse response = cartService.addToCart(request, guestToken);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart successfully", response));
    }

    @PutMapping("/items/{cartItemId}")
    @Operation(summary = "Update cart item quantity")
    @Timed(value = "cart.update", description = "Time taken to update cart item")
    public ResponseEntity<ApiResponse> updateCartItem(
            @PathVariable UUID cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            @RequestHeader(value = "X-Guest-Token", required = false) String headerGuestToken,
            @RequestParam(value = "guestToken", required = false) String paramGuestToken
    ) {
        String guestToken = headerGuestToken != null ? headerGuestToken : paramGuestToken;
        CartResponse response = cartService.updateCartItem(cartItemId, request, guestToken);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", response));
    }

    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "Remove item from cart")
    @Timed(value = "cart.remove", description = "Time taken to remove from cart")
    public ResponseEntity<ApiResponse> removeFromCart(
            @PathVariable UUID cartItemId,
            @RequestHeader(value = "X-Guest-Token", required = false) String headerGuestToken,
            @RequestParam(value = "guestToken", required = false) String paramGuestToken
    ) {
        String guestToken = headerGuestToken != null ? headerGuestToken : paramGuestToken;
        CartResponse response = cartService.removeFromCart(cartItemId, guestToken);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully", response));
    }

    @DeleteMapping
    @Operation(summary = "Clear cart")
    @Timed(value = "cart.clear", description = "Time taken to clear cart")
    public ResponseEntity<ApiResponse> clearCart(
            @RequestHeader(value = "X-Guest-Token", required = false) String headerGuestToken,
            @RequestParam(value = "guestToken", required = false) String paramGuestToken
    ) {
        String guestToken = headerGuestToken != null ? headerGuestToken : paramGuestToken;
        CartResponse response = cartService.clearCart(guestToken);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", response));
    }

    @PostMapping("/merge")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Merge guest cart with user cart on login")
    @Timed(value = "cart.merge", description = "Time taken to merge carts")
    public ResponseEntity<ApiResponse> mergeCart(
            @RequestHeader(value = "X-Guest-Token", required = false) String headerGuestToken,
            @RequestParam(value = "guestToken", required = false) String paramGuestToken,
            @RequestParam UUID userId
    ) {
        String guestToken = headerGuestToken != null ? headerGuestToken : paramGuestToken;
        CartResponse response = cartService.mergeGuestCart(guestToken, userId);
        return ResponseEntity.ok(ApiResponse.success("Cart merged successfully", response));
    }
}