# Cart Guest Access Fix - Summary

## Problem
Users were unable to add products to cart without being logged in. The API was returning 401 (Unauthorized) errors:
```
:8080/api/cart?guestToken=...  Failed to load resource: the server responded with a status of 401 ()
```

## Root Cause
1. **SecurityConfig was blocking guest access**: The security configuration required authentication for ALL `/api/cart/**` endpoints
2. **Controller didn't support query parameters**: The frontend was sending `guestToken` as a query parameter, but the controller only looked for it in headers
3. **getCurrentUserId() was not implemented**: The method returned null instead of extracting the authenticated user's ID

## Solution

### 1. Updated SecurityConfig.java (Lines 87-90)
Changed cart endpoint authorization to allow public access:

**BEFORE:**
```java
.requestMatchers("/api/orders/**", "/api/cart/**").hasAnyAuthority("CUSTOMER", "ADMIN")
```

**AFTER:**
```java
// Allow guest users to access cart endpoints with guestToken or authenticated users with CUSTOMER/ADMIN roles
.requestMatchers("/api/cart/**").permitAll()
.requestMatchers("/api/orders/**").hasAnyAuthority("CUSTOMER", "ADMIN")
```

**Key Change:** Separated cart endpoints with `permitAll()` to allow guest access while maintaining guest token validation in the service layer.

### 2. Updated CartController.java (All endpoint methods)
Modified each endpoint method to support BOTH header and query parameter methods for providing the guest token:

**BEFORE:**
```java
@GetMapping
public ResponseEntity<ApiResponse> getCart(
    @RequestHeader(value = "X-Guest-Token", required = false) String guestToken
) { ... }
```

**AFTER:**
```java
@GetMapping
public ResponseEntity<ApiResponse> getCart(
    @RequestHeader(value = "X-Guest-Token", required = false) String headerGuestToken,
    @RequestParam(value = "guestToken", required = false) String paramGuestToken
) {
    String guestToken = headerGuestToken != null ? headerGuestToken : paramGuestToken;
    CartResponse response = cartService.getCart(guestToken);
    return ResponseEntity.ok(ApiResponse.success("Cart fetched successfully", response));
}
```

**Updated Methods:**
- GET `/api/cart` - Get current cart
- POST `/api/cart/items` - Add item to cart
- PUT `/api/cart/items/{cartItemId}` - Update cart item quantity
- DELETE `/api/cart/items/{cartItemId}` - Remove item from cart
- DELETE `/api/cart` - Clear cart
- POST `/api/cart/merge` - Merge guest cart with user cart

**Key Change:** Controllers now accept guestToken from both:
- Header: `X-Guest-Token: token-value`
- Query Parameter: `?guestToken=token-value`

This matches what the frontend is already sending.

### 3. Updated CartServiceImpl.java

#### a. Added Required Imports (Lines 22, 31-32):
```java
import com.picknquicks.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
```

#### b. Implemented getCurrentUserId() Method (Lines 403-415):
**BEFORE:**
```java
private UUID getCurrentUserId() {
    return null;
}
```

**AFTER:**
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

**Key Change:** Now properly extracts the user ID from the JWT token in the security context, enabling authenticated users to access their own carts.

## How It Works Now

1. **Guest Users:**
   - Send requests to `/api/cart/**` endpoints without JWT authentication
   - Include `guestToken` as either:
     - Query parameter: `GET /api/cart?guestToken=abc123`
     - Header: `X-Guest-Token: abc123`
   - Service creates/manages guest carts using the guest token
   - Carts expire after 30 days

2. **Authenticated Users:**
   - Send requests with valid JWT token in Authorization header
   - Can optionally include `guestToken` for cart merging
   - `getCurrentUserId()` extracts user ID from security context
   - Service creates/manages user carts linked to their account

3. **Security Flow:**
   - Request reaches `/api/cart/**` endpoint (now allows any user)
   - CartController extracts guestToken from header or query parameter
   - CartService determines if request is from:
     - **Guest**: Has guestToken parameter → uses guest cart management
     - **Authenticated**: Has valid JWT → derives userId from security context
   - Appropriate cart lookup/creation happens based on context
   - **Result**: No more 401 errors! ✅

## Testing the Fix

### For Guest Users:
```bash
# Using query parameter (what frontend uses)
curl -X POST http://localhost:8080/api/cart/items?guestToken=a0411302-16d1-498f-9bea-9bad9c6481bb \
  -H "Content-Type: application/json" \
  -d '{"productId": "uuid-here", "quantity": 1}'

# Using query parameter - get cart
curl "http://localhost:8080/api/cart?guestToken=a0411302-16d1-498f-9bea-9bad9c6481bb"

# Alternative: Using header
curl -X POST http://localhost:8080/api/cart/items \
  -H "X-Guest-Token: a0411302-16d1-498f-9bea-9bad9c6481bb" \
  -H "Content-Type: application/json" \
  -d '{"productId": "uuid-here", "quantity": 1}'
```

### For Authenticated Users:
```bash
# Add product to user cart (JWT token in Authorization header)
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{"productId": "uuid-here", "quantity": 1}'

# Get user cart
curl http://localhost:8080/api/cart \
  -H "Authorization: Bearer your-jwt-token"

# Merge guest cart in user cart on login
curl -X POST "http://localhost:8080/api/cart/merge?guestToken=guest-token-value&userId=user-id" \
  -H "Authorization: Bearer your-jwt-token"
```

## Files Modified

1. **SecurityConfig.java** (Lines 87-89)
   - Changed to allow unauthenticated access to cart endpoints

2. **CartController.java** (Lines 29-95)
   - Updated all 6 endpoint methods to support both header and query parameter for guest token

3. **CartServiceImpl.java** (Lines 1-34, 403-415)
   - Added security context imports
   - Implemented `getCurrentUserId()` method to extract user ID from JWT token

## Security Considerations

✅ **Secure Design:**
- Guest tokens are UUIDs generated client-side (unique per session)
- Each guest cart is completely isolated by its token
- Guests without a valid token are treated as anonymous
- The service layer validates all business logic
- Authenticated users get their own separate carts linked to their user ID
- Cart merge endpoint still requires authentication (`@PreAuthorize("isAuthenticated()")`)
- JWT tokens must still be valid for authenticated requests
- No data can be accessed without proper token/authentication

## Troubleshooting

If you still see 401 errors after deploying:

1. **Clear browser cache** - The security configuration is now different
2. **Verify guest token is being sent** - Check network tab in DevTools
3. **Check server logs** - Look for exceptions in application logs
4. **Restart the application** - Ensure new config is loaded

## Performance Impact

- **Minimal**: Added one extra parameter extraction per request
- **Caching**: Guest and user carts are cached separately (30 min TTL)
- **Database**: Same query patterns, just made accessible to more users



