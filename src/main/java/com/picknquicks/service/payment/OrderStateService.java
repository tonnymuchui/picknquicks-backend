package com.picknquicks.service.payment;

import com.picknquicks.domain.order.Order;
import com.picknquicks.domain.order.OrderStatus;
import com.picknquicks.domain.order.PaymentMethod;
import com.picknquicks.domain.order.PaymentStatus;
import com.picknquicks.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for managing order state transitions based on payment status.
 * Follows Single Responsibility Principle: Only manages order state logic.
 * Decoupled from payment processing details.
 */
@Service
@Slf4j
public class OrderStateService {

    /**
     * Update order state when payment is successfully completed.
     * Different logic for different payment methods:
     * - M-Pesa: Order moves to PAID status immediately
     * - CASH_ON_DELIVERY: Order stays in PROCESSING (payment happens on delivery)
     */
    public void handlePaymentCompleted(Order order) {
        if (order.getPaymentMethod() == PaymentMethod.MPESA) {
            if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
                log.warn("Order {} has unexpected status {} for payment completion. Expected: PAYMENT_PENDING",
                    order.getOrderNumber(), order.getStatus());
            }

            order.setPaymentStatus(PaymentStatus.COMPLETED);
            order.updateStatus(OrderStatus.PAID);

            log.info("Order {} status updated to PAID after M-Pesa payment completion",
                order.getOrderNumber());
        } else if (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
            // For COD, this should NOT be called from M-Pesa callback
            throw new BadRequestException(
                "Cannot mark CASH_ON_DELIVERY order as PAID via M-Pesa callback. " +
                "M-Pesa should not handle COD orders."
            );
        }
    }

    /**
     * Update order state when payment fails.
     * Order transitions to PAYMENT_FAILED status.
     */
    public void handlePaymentFailed(Order order, String failureReason) {
        if (order.getPaymentMethod() == PaymentMethod.MPESA) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.updateStatus(OrderStatus.PAYMENT_FAILED);

            log.warn("Order {} payment failed: {}", order.getOrderNumber(), failureReason);
        } else if (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
            log.error("CASH_ON_DELIVERY order {} should not receive payment failure from M-Pesa",
                order.getOrderNumber());
        }
    }

    /**
     * Update order when payment is initiated (STK push sent)
     */
    public void handlePaymentInitiated(Order order) {
        if (order.getPaymentMethod() == PaymentMethod.MPESA) {
            order.updateStatus(OrderStatus.PAYMENT_PENDING);
            order.setPaymentStatus(PaymentStatus.PROCESSING);

            log.info("Order {} transitioned to PAYMENT_PENDING after M-Pesa STK push",
                order.getOrderNumber());
        }
    }

    /**
     * Validate that order can accept payment
     */
    public void validateOrderCanReceivePayment(Order order) {
        if (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
            throw new BadRequestException(
                "CASH_ON_DELIVERY orders should not have online payment callbacks. " +
                "Order: " + order.getOrderNumber()
            );
        }

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new BadRequestException(
                String.format("Order %s cannot receive payment in status: %s",
                    order.getOrderNumber(), order.getStatus())
            );
        }
    }
}

