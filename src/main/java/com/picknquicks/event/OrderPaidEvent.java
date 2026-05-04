package com.picknquicks.event;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderPaidEvent extends OrderEvent {

    private final String transactionId;
    private final BigDecimal amount;

    public OrderPaidEvent(Object source, UUID orderId, String transactionId, BigDecimal amount) {
        super(source, orderId, "ORDER_PAID");
        this.transactionId = transactionId;
        this.amount = amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}