package com.picknquicks.repository.cart;
import com.picknquicks.domain.cart.Cart;
import com.picknquicks.domain.cart.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByUserIdAndStatus(UUID userId, CartStatus status);

    Optional<Cart> findByGuestTokenAndStatus(String guestToken, CartStatus status);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.id = :id")
    Optional<Cart> findByIdWithItems(@Param("id") UUID id);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.user.id = :userId AND c.status = :status")
    Optional<Cart> findByUserIdAndStatusWithItems(@Param("userId") UUID userId, @Param("status") CartStatus status);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.guestToken = :guestToken AND c.status = :status")
    Optional<Cart> findByGuestTokenAndStatusWithItems(@Param("guestToken") String guestToken, @Param("status") CartStatus status);

    @Query("SELECT c FROM Cart c WHERE c.status = 'ACTIVE' AND c.lastActivityAt < :threshold AND c.items IS NOT EMPTY")
    List<Cart> findAbandonedCarts(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT c FROM Cart c WHERE c.status = 'ACTIVE' AND c.expiresAt < :now")
    List<Cart> findExpiredCarts(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Cart c SET c.status = :newStatus WHERE c.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("newStatus") CartStatus newStatus);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.expiresAt < :date AND c.status IN ('EXPIRED', 'CONVERTED', 'MERGED')")
    void deleteOldCarts(@Param("date") LocalDateTime date);

    @Query("SELECT COUNT(c) FROM Cart c WHERE c.status = 'ACTIVE'")
    Long countActiveCarts();

    @Query("SELECT COUNT(c) FROM Cart c WHERE c.status = 'ABANDONED'")
    Long countAbandonedCarts();
}