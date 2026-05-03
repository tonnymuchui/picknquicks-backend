package com.picknquicks.service.cart;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.picknquicks.domain.cart.Cart;
import com.picknquicks.domain.cart.CartItem;
import com.picknquicks.domain.cart.CartStatus;
import com.picknquicks.domain.product.Product;
import com.picknquicks.domain.user.User;
import com.picknquicks.dto.request.cart.AddToCartRequest;
import com.picknquicks.dto.request.cart.UpdateCartItemRequest;
import com.picknquicks.dto.response.cart.CartResponse;
import com.picknquicks.event.CartAbandonedEvent;
import com.picknquicks.event.CartItemAddedEvent;
import com.picknquicks.event.CartMergedEvent;
import com.picknquicks.exception.BadRequestException;
import com.picknquicks.exception.ResourceNotFoundException;
import com.picknquicks.mapper.CartMapper;
import com.picknquicks.repository.cart.CartItemRepository;
import com.picknquicks.repository.cart.CartRepository;
import com.picknquicks.repository.product.ProductRepository;
import com.picknquicks.repository.user.UserRepository;
import com.picknquicks.security.UserPrincipal;
import com.picknquicks.service.cart.CartService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CART_CACHE_PREFIX = "cart:";
    private static final long GUEST_CART_EXPIRY_DAYS = 30;
    private static final long CART_CACHE_TTL_MINUTES = 30;

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#guestToken != null ? #guestToken : 'user:' + authentication.principal.id")
    @CircuitBreaker(name = "cartService", fallbackMethod = "addToCartFallback")
    public CartResponse addToCart(AddToCartRequest request, String guestToken) {
        Product product = validateProduct(request.getProductId());
        validateStock(product, request.getQuantity());

        Cart cart = getOrCreateCart(guestToken);

        CartItem existingItem = cart.findItemByProductId(product.getId());

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            validateStock(product, newQuantity);
            existingItem.incrementQuantity(request.getQuantity());
            existingItem.updatePrice(product.getEffectivePrice());
        } else {
            CartItem newItem = CartItem.builder()
                    .product(product)
                    .quantity(request.getQuantity())
                    .price(product.getEffectivePrice())
                    .taxRate(product.getTaxRate() != null ? product.getTaxRate() : BigDecimal.ZERO)
                    .build();

            cart.addItem(newItem);
        }

        Cart savedCart = cartRepository.save(cart);

        cacheCart(savedCart, guestToken);

        eventPublisher.publishEvent(new CartItemAddedEvent(
                this, savedCart.getId(), product.getId(), request.getQuantity()
        ));

        log.info("Added {} x {} to cart {}", request.getQuantity(), product.getName(), savedCart.getId());

        return cartMapper.toCartResponse(savedCart);
    }
    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#guestToken != null ? #guestToken : 'user:' + authentication.principal.id")
    public CartResponse updateCartItem(UUID cartItemId, UpdateCartItemRequest request, String guestToken) {
        // ✅ FIX: use findCart() (returns Cart) not getCart() (returns CartResponse)
        Cart cart = findCart(guestToken);

        if (cart == null) {
            throw new ResourceNotFoundException("Cart not found");
        }

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        validateStock(cartItem.getProduct(), request.getQuantity());

        cartItem.setQuantity(request.getQuantity());
        cartItem.updatePrice(cartItem.getProduct().getEffectivePrice());
        cart.updateActivity();

        Cart savedCart = cartRepository.save(cart);

        cacheCart(savedCart, guestToken);

        log.info("Updated cart item {} quantity to {}", cartItemId, request.getQuantity());

        return cartMapper.toCartResponse(savedCart);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#guestToken != null ? #guestToken : 'user:' + authentication.principal.id")
    public CartResponse removeFromCart(UUID cartItemId, String guestToken) {
        // ✅ FIX: use findCart() not getCart()
        Cart cart = findCart(guestToken);

        if (cart == null) {
            throw new ResourceNotFoundException("Cart not found");
        }

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cart.removeItem(cartItem);

        Cart savedCart = cartRepository.save(cart);

        cacheCart(savedCart, guestToken);

        log.info("Removed cart item {} from cart {}", cartItemId, cart.getId());

        return cartMapper.toCartResponse(savedCart);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#guestToken != null ? #guestToken : 'user:' + authentication.principal.id")
    public CartResponse clearCart(String guestToken) {
        // ✅ FIX: use findCart() not getCart()
        Cart cart = findCart(guestToken);

        if (cart == null) {
            throw new ResourceNotFoundException("Cart not found");
        }

        cart.clearItems();

        Cart savedCart = cartRepository.save(cart);

        cacheCart(savedCart, guestToken);

        log.info("Cleared cart {}", cart.getId());

        return cartMapper.toCartResponse(savedCart);
    }
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "cart", key = "#guestToken != null ? #guestToken : 'user:' + authentication.principal.id")
    public CartResponse getCart(String guestToken) {
        Cart cart = findCart(guestToken);

        if (cart == null) {
            return CartResponse.builder()
                    .items(List.of())
                    .totalItems(0)
                    .subtotal(BigDecimal.ZERO)
                    .tax(BigDecimal.ZERO)
                    .total(BigDecimal.ZERO)
                    .isGuest(guestToken != null)
                    .build();
        }

        return cartMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", allEntries = true)
    public CartResponse mergeGuestCart(String guestToken, UUID userId) {
        if (guestToken == null) {
            return getOrCreateUserCart(userId);
        }

        Cart guestCart = cartRepository.findByGuestTokenAndStatusWithItems(guestToken, CartStatus.ACTIVE)
                .orElse(null);

        if (guestCart == null || !guestCart.hasItems()) {
            return getOrCreateUserCart(userId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart userCart = cartRepository.findByUserIdAndStatusWithItems(userId, CartStatus.ACTIVE)
                .orElseGet(() -> createUserCart(user));

        for (CartItem guestItem : guestCart.getItems()) {
            CartItem existingItem = userCart.findItemByProductId(guestItem.getProduct().getId());

            if (existingItem != null) {
                existingItem.incrementQuantity(guestItem.getQuantity());
                existingItem.updatePrice(guestItem.getProduct().getEffectivePrice());
            } else {
                CartItem newItem = CartItem.builder()
                        .product(guestItem.getProduct())
                        .quantity(guestItem.getQuantity())
                        .price(guestItem.getPrice())
                        .taxRate(guestItem.getTaxRate())
                        .build();

                userCart.addItem(newItem);
            }
        }

        guestCart.setStatus(CartStatus.MERGED);
        cartRepository.save(guestCart);

        Cart mergedCart = cartRepository.save(userCart);

        clearGuestCartCache(guestToken);

        eventPublisher.publishEvent(new CartMergedEvent(this, guestCart.getId(), mergedCart.getId()));

        log.info("Merged guest cart {} into user cart {}", guestCart.getId(), mergedCart.getId());

        return cartMapper.toCartResponse(mergedCart);
    }

    @Override
    @Transactional
    public void markAsAbandoned(UUID cartId) {
        Cart cart = cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getStatus() != CartStatus.ACTIVE || !cart.hasItems()) {
            return;
        }

        cart.setStatus(CartStatus.ABANDONED);
        cartRepository.save(cart);

        String email = cart.getUser() != null ? cart.getUser().getEmail() : null;

        if (email != null) {
            eventPublisher.publishEvent(new CartAbandonedEvent(
                    this, cart.getId(), email, cart.getTotalItems()
            ));

            log.info("Marked cart {} as abandoned, email sent to {}", cartId, email);
        } else {
            log.info("Marked guest cart {} as abandoned (no email)", cartId);
        }
    }

    @Override
    @Transactional
    public void cleanupExpiredCarts() {
        LocalDateTime now = LocalDateTime.now();
        List<Cart> expiredCarts = cartRepository.findExpiredCarts(now);

        expiredCarts.forEach(cart -> {
            cart.setStatus(CartStatus.EXPIRED);
            cartRepository.save(cart);
        });

        cartRepository.deleteOldCarts(now.minusDays(90));

        log.info("Cleaned up {} expired carts", expiredCarts.size());
    }

    private Cart getOrCreateCart(String guestToken) {
        if (guestToken != null) {
            return getOrCreateGuestCart(guestToken);
        }

        UUID userId = getCurrentUserId();
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            return getOrCreateUserCart(user);
        }

        throw new BadRequestException("Either guest token or authentication is required");
    }

    private Cart getOrCreateGuestCart(String guestToken) {
        return cartRepository.findByGuestTokenAndStatusWithItems(guestToken, CartStatus.ACTIVE)
                .orElseGet(() -> createGuestCart(guestToken));
    }

    private Cart getOrCreateUserCart(User user) {
        return cartRepository.findByUserIdAndStatusWithItems(user.getId(), CartStatus.ACTIVE)
                .orElseGet(() -> createUserCart(user));
    }

    private CartResponse getOrCreateUserCart(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = getOrCreateUserCart(user);
        return cartMapper.toCartResponse(cart);
    }

    private Cart createGuestCart(String guestToken) {
        Cart cart = Cart.builder()
                .guestToken(guestToken)
                .status(CartStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusDays(GUEST_CART_EXPIRY_DAYS))
                .lastActivityAt(LocalDateTime.now())
                .build();

        return cartRepository.save(cart);
    }

    private Cart createUserCart(User user) {
        Cart cart = Cart.builder()
                .user(user)
                .status(CartStatus.ACTIVE)
                .lastActivityAt(LocalDateTime.now())
                .build();

        return cartRepository.save(cart);
    }

    private Cart findCart(String guestToken) {
        if (guestToken != null) {
            return cartRepository.findByGuestTokenAndStatusWithItems(guestToken, CartStatus.ACTIVE)
                    .orElse(null);
        }

        UUID userId = getCurrentUserId();
        if (userId != null) {
            return cartRepository.findByUserIdAndStatusWithItems(userId, CartStatus.ACTIVE)
                    .orElse(null);
        }

        return null;
    }

    private Product validateProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getActive()) {
            throw new BadRequestException("Product is not active");
        }

        return product;
    }

    private void validateStock(Product product, int requestedQuantity) {
        if (!product.isInStock()) {
            throw new BadRequestException("Product is out of stock");
        }

        if (product.getStockQuantity() < requestedQuantity) {
            throw new BadRequestException(
                    String.format("Insufficient stock. Only %d available", product.getStockQuantity())
            );
        }
    }

    private void cacheCart(Cart cart, String guestToken) {
        try {
            String cacheKey = CART_CACHE_PREFIX + (guestToken != null ? guestToken : "user:" + cart.getUser().getId());
            CartResponse response = cartMapper.toCartResponse(cart);
            String json = objectMapper.writeValueAsString(response);

            redisTemplate.opsForValue().set(cacheKey, json, CART_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("Failed to cache cart", e);
        }
    }

    private void clearGuestCartCache(String guestToken) {
        String cacheKey = CART_CACHE_PREFIX + guestToken;
        redisTemplate.delete(cacheKey);
    }

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

    private CartResponse addToCartFallback(AddToCartRequest request, String guestToken, Exception ex) {
        log.error("Circuit breaker fallback for addToCart", ex);
        throw new BadRequestException("Cart service temporarily unavailable. Please try again later.");
    }
}