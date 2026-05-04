package com.picknquicks.domain.order;

public enum OrderStatus {
    PENDING,                // Order created, awaiting payment
    PAYMENT_PENDING,        // Payment initiated (M-Pesa STK sent)
    PAYMENT_FAILED,         // Payment failed
    PAID,                   // Payment confirmed
    PROCESSING,             // Order being prepared
    READY_TO_SHIP,          // Ready for pickup by courier
    SHIPPED,                // Out for delivery
    DELIVERED,              // Delivered to customer
    COMPLETED,              // Order completed (no returns)
    CANCELLED,              // Order cancelled
    REFUND_PENDING,         // Refund requested
    REFUNDED                // Refund processed
}