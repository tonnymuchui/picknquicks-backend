package com.picknquicks.event.listener;
import com.picknquicks.event.ProductCreatedEvent;
import com.picknquicks.event.ProductStockChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Async
    @EventListener
    public void handleProductCreated(ProductCreatedEvent event) {
        log.info("Product created event received for product: {}", event.getProductId());
        kafkaTemplate.send("product-created", event.getProductId().toString(), event);
    }

    @Async
    @EventListener
    public void handleProductStockChanged(ProductStockChangedEvent event) {
        log.info("Stock changed for product {}: {} -> {}",
                event.getProductId(), event.getOldStock(), event.getNewStock());

        kafkaTemplate.send("product-stock-changed", event.getProductId().toString(), event);

        if (event.getNewStock() <= 0) {
            log.warn("Product {} is now out of stock", event.getProductId());
        }
    }
}