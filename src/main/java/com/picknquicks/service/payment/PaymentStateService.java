package com.picknquicks.service.payment;

import com.picknquicks.domain.order.Payment;
import com.picknquicks.domain.order.PaymentStatus;
import com.picknquicks.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for managing payment state transitions.
 * Follows the State Pattern to ensure valid payment state transitions.
 * Single Responsibility: Manages payment state transitions only.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentStateService {

    /**
     * Transition payment to PROCESSING state
     */
    @Transactional
    public void transitionToProcessing(Payment payment, String checkoutRequestId, String merchantRequestId) {
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException(
                String.format("Cannot transition to PROCESSING from %s status", payment.getStatus())
            );
        }

        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setMpesaCheckoutRequestId(checkoutRequestId);
        payment.setMpesaMerchantRequestId(merchantRequestId);

        log.info("Payment {} transitioned to PROCESSING with checkout ID: {}",
            payment.getId(), checkoutRequestId);
    }

    /**
     * Transition payment to COMPLETED state with M-Pesa transaction details
     */
    @Transactional
    public void transitionToCompleted(Payment payment, String transactionId, String receiptNumber) {
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            log.warn("Payment {} is already COMPLETED. Ignoring duplicate completion.", payment.getId());
            return;
        }

        if (payment.getStatus() != PaymentStatus.PROCESSING) {
            throw new BadRequestException(
                String.format("Cannot complete payment from %s status. Expected: PROCESSING", payment.getStatus())
            );
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId(transactionId);
        payment.setMpesaReceiptNumber(receiptNumber);
        payment.setPaidAt(LocalDateTime.now());

        log.info("Payment {} transitioned to COMPLETED. Transaction ID: {}, Receipt: {}",
            payment.getId(), transactionId, receiptNumber);
    }

    /**
     * Transition payment to FAILED state with reason
     */
    @Transactional
    public void transitionToFailed(Payment payment, String failureReason) {
        if (payment.getStatus() == PaymentStatus.FAILED) {
            log.warn("Payment {} is already FAILED. Ignoring duplicate failure.", payment.getId());
            return;
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new BadRequestException(
                "Cannot fail a payment that has already been completed"
            );
        }

        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(failureReason);

        log.warn("Payment {} transitioned to FAILED. Reason: {}", payment.getId(), failureReason);
    }

    /**
     * Validate that payment can be transitioned based on current state
     */
    public boolean canTransitionToCompleted(Payment payment) {
        return payment.getStatus() == PaymentStatus.PROCESSING;
    }

    /**
     * Validate that payment is in expected state for callback processing
     */
    public boolean isValidForCallbackProcessing(Payment payment) {
        return payment.getStatus() == PaymentStatus.PROCESSING;
    }
}


