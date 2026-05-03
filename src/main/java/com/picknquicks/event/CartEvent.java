package com.picknquicks.event;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.util.UUID;

@Getter
public abstract class CartEvent extends ApplicationEvent {

    private final UUID cartId;
    private final String eventType;

    public CartEvent(Object source, UUID cartId, String eventType) {
        super(source);
        this.cartId = cartId;
        this.eventType = eventType;
    }
}