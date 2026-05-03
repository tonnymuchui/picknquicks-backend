package com.picknquicks.event.listener;
import com.picknquicks.event.CartAbandonedEvent;
import com.picknquicks.event.CartItemAddedEvent;
import com.picknquicks.event.CartMergedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartEventListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Async
    @EventListener
    public void handleCartItemAdded(CartItemAddedEvent event) {
        log.info("Cart item added event: cartId={}, productId={}, quantity={}",
                event.getCartId(), event.getProductId(), event.getQuantity());

        kafkaTemplate.send("cart-item-added", event.getCartId().toString(), event);
    }

    @Async
    @EventListener
    public void handleCartAbandoned(CartAbandonedEvent event) {
        log.info("Cart abandoned event: cartId={}, email={}, itemCount={}",
                event.getCartId(), event.getEmail(), event.getItemCount());

        kafkaTemplate.send("cart-abandoned", event.getCartId().toString(), event);

        log.info("Abandoned cart email queued for {}", event.getEmail());
    }

    @Async
    @EventListener
    public void handleCartMerged(CartMergedEvent event) {
        log.info("Cart merged event: guestCartId={}, userCartId={}",
                event.getGuestCartId(), event.getUserCartId());

        kafkaTemplate.send("cart-merged", event.getUserCartId().toString(), event);
    }
}