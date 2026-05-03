package com.picknquicks.domain.cart;

import com.picknquicks.domain.user.User;
import com.picknquicks.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "carts", indexes = {@Index(name = "idx_cart_user", columnList = "user_id"), @Index(name = "idx_cart_guest_token", columnList = "guest_token"), @Index(name = "idx_cart_status", columnList = "status"), @Index(name = "idx_cart_expires", columnList = "expires_at"), @Index(name = "idx_cart_updated", columnList = "updated_at")}, uniqueConstraints = {@UniqueConstraint(name = "uk_cart_user", columnNames = "user_id"), @UniqueConstraint(name = "uk_cart_guest_token", columnNames = "guest_token")})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "guest_token", length = 36)
    private String guestToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CartStatus status = CartStatus.ACTIVE;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<CartItem> items = new HashSet<>();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;


    public boolean isGuest() {
        return user == null && guestToken != null;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
        updateActivity();
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
        updateActivity();
    }

    public void clearItems() {
        items.clear();
        updateActivity();
    }

    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    public BigDecimal getSubtotal() {
        return items.stream().map(CartItem::getItemTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTax() {
        return items.stream().map(CartItem::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotal() {
        return getSubtotal().add(getTax());
    }

    public int getTotalItems() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public boolean hasItems() {
        return !items.isEmpty();
    }

    public CartItem findItemByProductId(UUID productId) {
        return items.stream().filter(item -> item.getProduct().getId().equals(productId)).findFirst().orElse(null);
    }
}