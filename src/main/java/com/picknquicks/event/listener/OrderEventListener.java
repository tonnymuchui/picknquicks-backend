package com.picknquicks.event.listener;

import com.picknquicks.event.OrderCreatedEvent;
import com.picknquicks.event.OrderPaidEvent;
import com.picknquicks.event.OrderStatusChangedEvent;
import com.picknquicks.repository.order.OrderRepository;
import com.picknquicks.service.notification.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final EmailService emailService;
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Processing OrderCreatedEvent for order: {}", event.getOrder().getOrderNumber());

        try {
            emailService.sendOrderConfirmationEmail(event.getOrder());

            kafkaTemplate.send("order-created", event.getOrderId().toString(), event);

            log.info("Order created event processed successfully: {}", event.getOrder().getOrderNumber());
        } catch (Exception e) {
            log.error("Error processing order created event", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderPaid(OrderPaidEvent event) {
        log.info("Processing OrderPaidEvent for order ID: {}", event.getOrderId());

        try {
            orderRepository.findByIdWithDetails(event.getOrderId()).ifPresent(emailService::sendPaymentConfirmationEmail);

            kafkaTemplate.send("order-paid", event.getOrderId().toString(), event);

            log.info("Order paid event processed successfully: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Error processing order paid event", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("Processing OrderStatusChangedEvent: {} -> {}",
                event.getOldStatus(), event.getNewStatus());

        try {
            orderRepository.findByIdWithDetails(event.getOrderId()).ifPresent(order -> {
                switch (event.getNewStatus()) {
                    case SHIPPED:
                        emailService.sendOrderShippedEmail(order);
                        break;
                    case DELIVERED:
                        emailService.sendOrderDeliveredEmail(order);
                        break;
                    case CANCELLED:
                        emailService.sendOrderCancelledEmail(order);
                        break;
                    default:
                        break;
                }
            });

            kafkaTemplate.send("order-status-changed", event.getOrderId().toString(), event);

            log.info("Order status changed event processed successfully: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Error processing order status changed event", e);
        }
    }
}