# Quick Reference - Exact Changes Made

## File 1: SecurityConfig.java
**Location**: `/home/tonny-muchui/IdeaProjects/picknquicks/src/main/java/com/picknquicks/config/SecurityConfig.java`

**Lines Changed**: 87-89 (within `securityFilterChain()` method)

### Before:
```java
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/orders/**", "/api/cart/**").hasAnyAuthority("CUSTOMER", "ADMIN")
                        .anyRequest().authenticated()
```

### After:
```java
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        // Allow guest users to access cart endpoints with guestToken or authenticated users with CUSTOMER/ADMIN roles
                        .requestMatchers("/api/cart/**").permitAll()
                        .requestMatchers("/api/orders/**").hasAnyAuthority("CUSTOMER", "ADMIN")
                        .anyRequest().authenticated()
```

---

## File 2: CartController.java
**Location**: `/home/tonny-muchui/IdeaProjects/picknquicks/src/main/java/com/picknquicks/controller/CartController.java`

**Method 1: getCart() - Lines 29-39**
```java
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
```

**Method 2: addToCart() - Lines 41-53**
```java
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
```

**Method 3: updateCartItem() - Lines 55-67**
```java
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
```

**Method 4: removeFromCart() - Lines 69-81**
```java
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
```

**Method 5: clearCart() - Lines 83-95**
```java
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
```

**Method 6: mergeCart() - Lines 97-107**
```java
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
```

---

## File 3: CartServiceImpl.java
**Location**: `/home/tonny-muchui/IdeaProjects/picknquicks/src/main/java/com/picknquicks/service/cart/CartServiceImpl.java`

### Import Additions (Lines 22, 31-32):
```java
import com.picknquicks.security.UserPrincipal;  // Add this import
import org.springframework.security.core.Authentication;  // Add this import
import org.springframework.security.core.context.SecurityContextHolder;  // Add this import
```

### Method Implementation (Lines 403-415):
Replace the stub:
```java
private UUID getCurrentUserId() {
    return null;
}
```

With:
```java
private UUID getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
        return null;
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof UserPrincipal) {
        return ((UserPrincipal) principal).getUser().getId();
    }

    return null;
}
```

---

## Summary

**Total Files Modified**: 3
**Total Lines Changed**: ~70 lines
**Compilation Status**: ✅ SUCCESS
**Breaking Changes**: ❌ NONE
**Database Migrations**: ❌ NONE

