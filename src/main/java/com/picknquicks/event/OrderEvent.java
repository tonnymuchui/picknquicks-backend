package com.picknquicks.event;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.util.UUID;

@Getter
public abstract class OrderEvent extends ApplicationEvent {

    private final UUID orderId;
    private final String eventType;

    public OrderEvent(Object source, UUID orderId, String eventType) {
        super(source);
        this.orderId = orderId;
        this.eventType = eventType;
    }
}