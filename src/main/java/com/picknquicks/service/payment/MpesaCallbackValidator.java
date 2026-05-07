package com.picknquicks.service.payment;

import com.picknquicks.domain.order.Payment;
import com.picknquicks.domain.order.PaymentStatus;
import com.picknquicks.dto.mpesa.MpesaCallbackRequest;
import com.picknquicks.exception.BadRequestException;
import com.picknquicks.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service for validating M-Pesa callbacks.
 * Ensures callback data integrity and handles idempotency.
 * Single Responsibility: Only validates callbacks.
 */
@Service
@Slf4j
public class MpesaCallbackValidator {

    /**
     * Validate callback structure and extract STK callback
     */
    public MpesaCallbackRequest.StkCallback extractAndValidate(MpesaCallbackRequest callback) {
        if (callback == null || callback.getBody() == null) {
            throw new BadRequestException("Invalid callback: Missing body");
        }

        MpesaCallbackRequest.StkCallback stkCallback = callback.getBody().getStkCallback();
        if (stkCallback == null) {
            throw new BadRequestException("Invalid callback: Missing stkCallback");
        }

        return stkCallback;
    }

    /**
     * Validate checkout request ID is not null and not empty
     */
    public void validateCheckoutRequestId(String checkoutRequestId) {
        if (checkoutRequestId == null || checkoutRequestId.isBlank()) {
            throw new BadRequestException("Invalid callback: Missing CheckoutRequestID");
        }
    }

    /**
     * Validate that the payment exists and is in correct state for callback processing
     */
    public void validatePaymentFound(Payment payment, String checkoutRequestId) {
        if (payment == null) {
            throw new ResourceNotFoundException(
                "Payment not found for checkout request: " + checkoutRequestId
            );
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            log.warn("Callback received for already COMPLETED payment: {}. Treating as idempotent.",
                payment.getId());
        } else if (payment.getStatus() != PaymentStatus.PROCESSING) {
            throw new BadRequestException(
                String.format(
                    "Payment %s is in invalid state for callback: %s. Expected: PROCESSING",
                    payment.getId(),
                    payment.getStatus()
                )
            );
        }
    }

    /**
     * Extract and validate metadata from callback
     * Required fields: MpesaReceiptNumber, TransactionId
     */
    public CallbackMetadataBundle extractMetadata(MpesaCallbackRequest.StkCallback stkCallback) {
        if (stkCallback.getCallbackMetadata() == null || stkCallback.getCallbackMetadata().getItem() == null) {
            throw new BadRequestException("Invalid callback: Missing callback metadata");
        }

        String mpesaReceiptNumber = extractMetadataValue(stkCallback.getCallbackMetadata(), "MpesaReceiptNumber");
        String transactionId = extractMetadataValue(stkCallback.getCallbackMetadata(), "TransactionId");
        String amountValue = extractMetadataValue(stkCallback.getCallbackMetadata(), "Amount");

        if (mpesaReceiptNumber == null || transactionId == null) {
            throw new BadRequestException(
                "Invalid callback: Missing receipt number or transaction ID in metadata"
            );
        }

        return new CallbackMetadataBundle(mpesaReceiptNumber, transactionId, amountValue);
    }

    /**
     * Validate payment amount matches expected amount
     */
    public void validatePaymentAmount(Payment payment, String amountString) {
        if (amountString == null || amountString.isBlank()) {
            log.warn("Payment {} callback missing amount field. Skipping amount validation.", payment.getId());
            return;
        }

        try {
            BigDecimal paidAmount = new BigDecimal(amountString);
            if (paidAmount.compareTo(payment.getAmount()) != 0) {
                throw new BadRequestException(
                    String.format(
                        "Payment amount mismatch for payment %s. Expected: %s, Received: %s",
                        payment.getId(),
                        payment.getAmount(),
                        paidAmount
                    )
                );
            }
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid payment amount in callback: " + amountString);
        }
    }

    /**
     * Check if callback represents a successful payment (resultCode = 0)
     */
    public boolean isSuccessfulPayment(MpesaCallbackRequest.StkCallback stkCallback) {
        return stkCallback.getResultCode() != null && stkCallback.getResultCode() == 0;
    }

    /**
     * Extract metadata value by name from callback metadata
     */
    private String extractMetadataValue(MpesaCallbackRequest.CallbackMetadata metadata, String name) {
        if (metadata == null || metadata.getItem() == null) {
            return null;
        }

        return metadata.getItem().stream()
                .filter(item -> name.equals(item.getName()))
                .map(item -> String.valueOf(item.getValue()))
                .findFirst()
                .orElse(null);
    }

    /**
     * DTO for callback metadata
     */
    public static class CallbackMetadataBundle {
        public final String mpesaReceiptNumber;
        public final String transactionId;
        public final String amountString;

        public CallbackMetadataBundle(String mpesaReceiptNumber, String transactionId, String amountString) {
            this.mpesaReceiptNumber = mpesaReceiptNumber;
            this.transactionId = transactionId;
            this.amountString = amountString;
        }
    }
}


