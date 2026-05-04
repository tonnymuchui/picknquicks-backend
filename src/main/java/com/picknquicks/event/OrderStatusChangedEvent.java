package com.picknquicks.event;
import com.picknquicks.domain.order.OrderStatus;

import java.util.UUID;

public class OrderStatusChangedEvent extends OrderEvent {

    private final OrderStatus oldStatus;
    private final OrderStatus newStatus;

    public OrderStatusChangedEvent(Object source, UUID orderId, OrderStatus oldStatus, OrderStatus newStatus) {
        super(source, orderId, "ORDER_STATUS_CHANGED");
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public OrderStatus getOldStatus() {
        return oldStatus;
    }

    public OrderStatus getNewStatus() {
        return newStatus;
    }
}