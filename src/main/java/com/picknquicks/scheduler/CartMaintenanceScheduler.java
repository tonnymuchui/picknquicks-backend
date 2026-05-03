package com.picknquicks.scheduler;
import com.picknquicks.domain.cart.Cart;
import com.picknquicks.repository.cart.CartRepository;
import com.picknquicks.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartMaintenanceScheduler {

    private final CartRepository cartRepository;
    private final CartService cartService;

    @Scheduled(cron = "0 0 */6 * * *")
    public void markAbandonedCarts() {
        log.info("Starting abandoned cart detection");

        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<Cart> abandonedCarts = cartRepository.findAbandonedCarts(threshold);

        int count = 0;
        for (Cart cart : abandonedCarts) {
            try {
                cartService.markAsAbandoned(cart.getId());
                count++;
            } catch (Exception e) {
                log.error("Failed to mark cart {} as abandoned", cart.getId(), e);
            }
        }

        log.info("Marked {} carts as abandoned", count);
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredCarts() {
        log.info("Starting expired cart cleanup");
        cartService.cleanupExpiredCarts();
        log.info("Expired cart cleanup completed");
    }
}