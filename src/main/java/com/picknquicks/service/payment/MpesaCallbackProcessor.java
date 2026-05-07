package com.picknquicks.service.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.picknquicks.domain.order.Order;
import com.picknquicks.domain.order.Payment;
import com.picknquicks.dto.mpesa.MpesaCallbackRequest;
import com.picknquicks.event.OrderPaidEvent;
import com.picknquicks.repository.order.OrderRepository;
import com.picknquicks.repository.order.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for M-Pesa callback processing.
 * Orchestrates the callback flow using separate services.
 * Single Responsibility: Coordinates callback processing workflow.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaCallbackProcessor {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final MpesaCallbackValidator callbackValidator;
    private final PaymentStateService paymentStateService;
    private final OrderStateService orderStateService;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    /**
     * Process M-Pesa STK callback.
     * Follows these steps:
     * 1. Extract and validate callback structure
     * 2. Find and validate payment in system
     * 3. Validate order can receive this callback
     * 4. Update payment and order states accordingly
     * 5. Publish domain events for downstream processing
     */
    @Transactional
    public void processCallback(MpesaCallbackRequest callback) {
        try {
            // Step 1: Extract and validate callback
            MpesaCallbackRequest.StkCallback stkCallback = callbackValidator.extractAndValidate(callback);
            String checkoutRequestId = stkCallback.getCheckoutRequestID();
            callbackValidator.validateCheckoutRequestId(checkoutRequestId);

            // Step 2: Find and validate payment exists
            Payment payment = paymentRepository.findByMpesaCheckoutRequestId(checkoutRequestId)
                    .orElseThrow(() -> new RuntimeException(
                        "Payment not found for checkout request: " + checkoutRequestId
                    ));
            callbackValidator.validatePaymentFound(payment, checkoutRequestId);

            // Get order and validate it can receive this callback
            Order order = orderRepository.findByIdWithLock(payment.getOrder().getId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            callbackValidator.validatePaymentFound(payment, checkoutRequestId);
            orderStateService.validateOrderCanReceivePayment(order);

            // Store callback data for audit trail
            payment.setCallbackData(objectMapper.writeValueAsString(callback));

            // Step 3: Handle callback result
            if (callbackValidator.isSuccessfulPayment(stkCallback)) {
                handleSuccessfulPayment(payment, order, stkCallback);
            } else {
                handleFailedPayment(payment, order, stkCallback);
            }

        } catch (Exception e) {
            log.error("Error processing M-Pesa callback", e);
            throw new RuntimeException("Failed to process callback", e);
        }
    }

    /**
     * Handle successful payment callback (resultCode == 0)
     */
    private void handleSuccessfulPayment(
            Payment payment,
            Order order,
            MpesaCallbackRequest.StkCallback stkCallback) {

        // Extract metadata
        MpesaCallbackValidator.CallbackMetadataBundle metadata =
            callbackValidator.extractMetadata(stkCallback);

        // Validate payment amount if provided
        callbackValidator.validatePaymentAmount(payment, metadata.amountString);

        // Update payment state to COMPLETED
        paymentStateService.transitionToCompleted(
            payment,
            metadata.transactionId,
            metadata.mpesaReceiptNumber
        );
        paymentRepository.save(payment);

        // Update order state
        orderStateService.handlePaymentCompleted(order);
        orderRepository.save(order);

        // Publish events for downstream processing
        eventPublisher.publishEvent(new OrderPaidEvent(
            this,
            order.getId(),
            metadata.transactionId,
            payment.getAmount()
        ));

        log.info("✅ Payment completed successfully for order {}: Receipt {}",
            order.getOrderNumber(), metadata.mpesaReceiptNumber);
    }

    /**
     * Handle failed payment callback (resultCode != 0)
     */
    private void handleFailedPayment(
            Payment payment,
            Order order,
            MpesaCallbackRequest.StkCallback stkCallback) {

        String failureReason = stkCallback.getResultDesc() != null
            ? stkCallback.getResultDesc()
            : "Unknown failure reason";

        // Update payment state to FAILED
        paymentStateService.transitionToFailed(payment, failureReason);
        paymentRepository.save(payment);

        // Update order state
        orderStateService.handlePaymentFailed(order, failureReason);
        orderRepository.save(order);

        log.error("❌ Payment failed for order {}: {}", order.getOrderNumber(), failureReason);
    }
}


