package com.picknquicks.event;

import com.picknquicks.domain.order.Order;

public class OrderCreatedEvent extends OrderEvent {

    private final Order order;

    public OrderCreatedEvent(Object source, Order order) {
        super(source, order.getId(), "ORDER_CREATED");
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }
}