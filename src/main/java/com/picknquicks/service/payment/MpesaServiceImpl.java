package com.picknquicks.service.payment;

import com.picknquicks.config.MpesaConfig;
import com.picknquicks.domain.order.Order;
import com.picknquicks.domain.order.Payment;
import com.picknquicks.domain.order.PaymentStatus;
import com.picknquicks.dto.mpesa.MpesaCallbackRequest;
import com.picknquicks.dto.mpesa.MpesaStkPushRequest;
import com.picknquicks.dto.mpesa.MpesaStkPushResponse;
import com.picknquicks.exception.BadRequestException;
import com.picknquicks.exception.ResourceNotFoundException;
import com.picknquicks.repository.order.OrderRepository;
import com.picknquicks.repository.order.PaymentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * M-Pesa payment service implementation.
 * Handles M-Pesa STK push initiation and callback routing.
 * Delegates to specialized services for specific concerns (authentication, callback processing, etc.)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaServiceImpl implements MpesaService {

    private final MpesaConfig mpesaConfig;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;
    private final MpesaAuthenticationService authenticationService;
    private final PaymentStateService paymentStateService;
    private final OrderStateService orderStateService;
    private final MpesaCallbackProcessor callbackProcessor;

    @Override
    @Transactional
    @CircuitBreaker(name = "mpesaService", fallbackMethod = "initiateStkPushFallback")
    @Retry(name = "mpesaPayment")
    public MpesaStkPushResponse initiateStkPush(UUID orderId, String phoneNumber, BigDecimal amount) {
        // Fetch order with details
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Validate order and payment
        Payment payment = validateOrderAndPayment(order);

        // Format phone number for M-Pesa
        String formattedPhone = authenticationService.formatPhoneNumber(phoneNumber);

        // Obtain access token
        String accessToken = authenticationService.obtainAccessToken();
        String timestamp = authenticationService.generateTimestamp();
        String password = authenticationService.generatePassword(timestamp);

        // Build STK push request
        MpesaStkPushRequest request = MpesaStkPushRequest.builder()
                .businessShortCode(mpesaConfig.getShortCode())
                .password(password)
                .timestamp(timestamp)
                .transactionType("CustomerPayBillOnline")
                .amount(String.valueOf(amount.intValue()))
                .partyA(formattedPhone)
                .partyB(mpesaConfig.getShortCode())
                .phoneNumber(formattedPhone)
                .callBackURL(mpesaConfig.getCallbackUrl())
                .accountReference(order.getOrderNumber())
                .transactionDesc("Payment for Order " + order.getOrderNumber())
                .build();

        // Send to M-Pesa API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        HttpEntity<MpesaStkPushRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<MpesaStkPushResponse> response = restTemplate.exchange(
                    mpesaConfig.getStkPushUrl(),
                    HttpMethod.POST,
                    entity,
                    MpesaStkPushResponse.class
            );

            MpesaStkPushResponse stkResponse = response.getBody();
            log.debug("M-Pesa STK Push Response Code: {}, Description: {}",
                    stkResponse != null ? stkResponse.getResponseCode() : "null",
                    stkResponse != null ? stkResponse.getResponseDescription() : "null");

            // Handle response
            if (stkResponse != null && "0".equals(stkResponse.getResponseCode())) {
                // Transition payment and order to PROCESSING state
                paymentStateService.transitionToProcessing(
                    payment,
                    stkResponse.getCheckoutRequestID(),
                    stkResponse.getMerchantRequestID()
                );
                paymentRepository.save(payment);

                // Update order to PAYMENT_PENDING
                orderStateService.handlePaymentInitiated(order);
                orderRepository.save(order);

                log.info("✅ STK Push initiated successfully for order {}: Checkout ID: {}",
                        order.getOrderNumber(), stkResponse.getCheckoutRequestID());
            } else {
                // Mark payment as FAILED
                paymentStateService.transitionToFailed(
                    payment,
                    "STK Push failed: " + (stkResponse != null ? stkResponse.getResponseDescription() : "Unknown error")
                );
                paymentRepository.save(payment);

                log.error("❌ STK Push failed for order {}: {}", order.getOrderNumber(),
                        stkResponse != null ? stkResponse.getResponseDescription() : "Unknown error");
            }

            return stkResponse;
        } catch (Exception e) {
            log.error("❌ Exception during STK Push for order {}: {}", order.getOrderNumber(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Validate order and payment are in correct state for STK push
     */
    private Payment validateOrderAndPayment(Order order) {
        Payment payment = order.getPayment();

        if (payment == null) {
            throw new BadRequestException("Payment not initialized for order: " + order.getOrderNumber());
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new BadRequestException("Payment already completed for order: " + order.getOrderNumber());
        }

        if (payment.getStatus() == PaymentStatus.PROCESSING) {
            throw new BadRequestException("Payment already in process for order: " + order.getOrderNumber());
        }

        return payment;
    }

    @Override
    @Transactional
    public void handleCallback(MpesaCallbackRequest callback) {
        // Delegate callback processing to specialized processor
        callbackProcessor.processCallback(callback);
    }

    @Override
    public String queryTransaction(String checkoutRequestId) {
        return "Transaction query not implemented yet";
    }

    /**
     * Fallback method for circuit breaker when M-Pesa service is unavailable
     */
    private MpesaStkPushResponse initiateStkPushFallback(
            UUID orderId,
            String phoneNumber,
            BigDecimal amount,
            Exception ex) {

        log.error("🔴 CIRCUIT BREAKER FALLBACK: M-Pesa STK Push failed for order ID: {}", orderId, ex);

        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        if (payment != null) {
            paymentStateService.transitionToFailed(
                payment,
                "M-Pesa service unavailable: " + ex.getMessage()
            );
            paymentRepository.save(payment);
            log.error("Payment marked as FAILED for order: {}", payment.getOrder().getOrderNumber());
        }

        throw new BadRequestException(
            "⚠️ Payment service temporarily unavailable. Please try again later. Error: " + ex.getMessage()
        );
    }
}
