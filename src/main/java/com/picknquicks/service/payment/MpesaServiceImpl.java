package com.picknquicks.service.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.picknquicks.config.MpesaConfig;
import com.picknquicks.domain.order.Order;
import com.picknquicks.domain.order.Payment;
import com.picknquicks.domain.order.PaymentStatus;
import com.picknquicks.dto.mpesa.MpesaCallbackRequest;
import com.picknquicks.dto.mpesa.MpesaStkPushRequest;
import com.picknquicks.dto.mpesa.MpesaStkPushResponse;
import com.picknquicks.event.OrderPaidEvent;
import com.picknquicks.exception.BadRequestException;
import com.picknquicks.exception.ResourceNotFoundException;
import com.picknquicks.repository.order.OrderRepository;
import com.picknquicks.repository.order.PaymentRepository;
import com.picknquicks.service.payment.MpesaService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaServiceImpl implements MpesaService {

    private final MpesaConfig mpesaConfig;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    @CircuitBreaker(name = "mpesaService", fallbackMethod = "initiateStkPushFallback")
    @Retry(name = "mpesaPayment")
    public MpesaStkPushResponse initiateStkPush(UUID orderId, String phoneNumber, BigDecimal amount) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Payment payment = order.getPayment();
        if (payment == null) {
            throw new BadRequestException("Payment not initialized");
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new BadRequestException("Payment already completed");
        }

        String accessToken = getAccessToken();
        String timestamp = generateTimestamp();
        String password = generatePassword(timestamp);

        String formattedPhone = formatPhoneNumber(phoneNumber);

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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        HttpEntity<MpesaStkPushRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<MpesaStkPushResponse> response = restTemplate.exchange(
                mpesaConfig.getStkPushUrl(),
                HttpMethod.POST,
                entity,
                MpesaStkPushResponse.class
        );

        MpesaStkPushResponse stkResponse = response.getBody();

        if (stkResponse != null && "0".equals(stkResponse.getResponseCode())) {
            payment.setStatus(PaymentStatus.PROCESSING);
            payment.setMpesaCheckoutRequestId(stkResponse.getCheckoutRequestID());
            payment.setMpesaMerchantRequestId(stkResponse.getMerchantRequestID());
            paymentRepository.save(payment);

            log.info("STK Push initiated for order {}: {}", order.getOrderNumber(), stkResponse.getCheckoutRequestID());
        } else {
            payment.markAsFailed("STK Push failed: " + (stkResponse != null ? stkResponse.getResponseDescription() : "Unknown error"));
            paymentRepository.save(payment);

            log.error("STK Push failed for order {}: {}", order.getOrderNumber(),
                    stkResponse != null ? stkResponse.getResponseDescription() : "Unknown error");
        }

        return stkResponse;
    }

    @Override
    @Transactional
    public void handleCallback(MpesaCallbackRequest callback) {
        try {
            var stkCallback = callback.getBody().getStkCallback();
            String checkoutRequestId = stkCallback.getCheckoutRequestID();

            Payment payment = paymentRepository.findByMpesaCheckoutRequestId(checkoutRequestId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found for checkout request: " + checkoutRequestId));

            Order order = payment.getOrder();

            payment.setCallbackData(objectMapper.writeValueAsString(callback));

            if (stkCallback.getResultCode() == 0) {
                var metadata = stkCallback.getCallbackMetadata();
                String mpesaReceiptNumber = extractMetadataValue(metadata, "MpesaReceiptNumber");
                String transactionId = extractMetadataValue(metadata, "TransactionId");

                payment.markAsCompleted(transactionId, mpesaReceiptNumber);
                paymentRepository.save(payment);

                eventPublisher.publishEvent(new OrderPaidEvent(
                        this, order.getId(), transactionId, payment.getAmount()
                ));

                log.info("Payment completed for order {}: Receipt {}", order.getOrderNumber(), mpesaReceiptNumber);
            } else {
                String failureReason = stkCallback.getResultDesc();
                payment.markAsFailed(failureReason);
                paymentRepository.save(payment);

                log.error("Payment failed for order {}: {}", order.getOrderNumber(), failureReason);
            }
        } catch (Exception e) {
            log.error("Error processing M-Pesa callback", e);
            throw new RuntimeException("Failed to process callback", e);
        }
    }

    @Override
    public String queryTransaction(String checkoutRequestId) {
        return "Transaction query not implemented yet";
    }

    private String getAccessToken() {
        String auth = mpesaConfig.getConsumerKey() + ":" + mpesaConfig.getConsumerSecret();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(encodedAuth);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(
                mpesaConfig.getAuthUrl(),
                HttpMethod.GET,
                entity,
                AccessTokenResponse.class
        );

        if (response.getBody() != null) {
            return response.getBody().getAccessToken();
        }

        throw new RuntimeException("Failed to get M-Pesa access token");
    }

    private String generateTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String generatePassword(String timestamp) {
        String str = mpesaConfig.getShortCode() + mpesaConfig.getPassKey() + timestamp;
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    private String formatPhoneNumber(String phone) {
        if (phone.startsWith("+254")) {
            return phone.substring(1);
        } else if (phone.startsWith("0")) {
            return "254" + phone.substring(1);
        } else if (phone.startsWith("254")) {
            return phone;
        }
        return "254" + phone;
    }

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

    private MpesaStkPushResponse initiateStkPushFallback(UUID orderId, String phoneNumber, BigDecimal amount, Exception ex) {
        log.error("Circuit breaker fallback for M-Pesa STK Push", ex);

        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        if (payment != null) {
            payment.markAsFailed("M-Pesa service unavailable. Please try again later.");
            paymentRepository.save(payment);
        }

        throw new BadRequestException("Payment service temporarily unavailable. Please try again later.");
    }

    private static class AccessTokenResponse {
        private String access_token;

        public String getAccessToken() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }
    }
}