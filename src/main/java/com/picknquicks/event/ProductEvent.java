package com.picknquicks.event;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.util.UUID;

@Getter
public abstract class ProductEvent extends ApplicationEvent {

    private final UUID productId;
    private final String eventType;

    public ProductEvent(Object source, UUID productId, String eventType) {
        super(source);
        this.productId = productId;
        this.eventType = eventType;
    }
}