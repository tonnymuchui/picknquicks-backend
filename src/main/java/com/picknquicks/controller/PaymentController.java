package com.picknquicks.controller;
import com.picknquicks.dto.mpesa.MpesaCallbackRequest;
import com.picknquicks.dto.mpesa.MpesaStkPushResponse;
import com.picknquicks.dto.request.InitiateMpesaPaymentRequest;
import com.picknquicks.dto.response.ApiResponse;
import com.picknquicks.service.payment.MpesaService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment processing endpoints")
public class PaymentController {

    private final MpesaService mpesaService;

    @PostMapping("/mpesa/initiate")
    @Operation(summary = "Initiate M-Pesa STK Push payment", description = "Send M-Pesa payment request for an order")
    @Timed(value = "payment.mpesa.initiate", description = "Time taken to initiate M-Pesa payment")
    public ResponseEntity<ApiResponse> initiateMpesaPayment(
            @Valid @RequestBody InitiateMpesaPaymentRequest request
    ) {
        log.info("Initiating M-Pesa payment for order: {}", request.getOrderId());

        try {
            MpesaStkPushResponse response = mpesaService.initiateStkPush(
                    request.getOrderId(),
                    request.getPhoneNumber(),
                    request.getAmount()
            );

            if (response != null && "0".equals(response.getResponseCode())) {
                log.info("✅ M-Pesa payment initiated successfully. Checkout ID: {}",
                    response.getCheckoutRequestID());

                return ResponseEntity.status(HttpStatus.OK)
                        .body(ApiResponse.success(
                            "M-Pesa payment initiated. Please enter your PIN on your phone.",
                            response
                        ));
            } else {
                String errorMsg = response != null ? response.getResponseDescription() : "Unknown error";
                log.error("❌ M-Pesa payment initiation failed: {}", errorMsg);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Payment initiation failed: " + errorMsg));
            }
        } catch (Exception e) {
            log.error("❌ Exception during M-Pesa payment initiation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Payment service error: " + e.getMessage()));
        }
    }
    @Operation(summary = "M-Pesa STK Push callback", description = "Webhook endpoint for M-Pesa payment notifications")
    public ResponseEntity<Map<String, String>> handleMpesaCallback(@RequestBody MpesaCallbackRequest callback) {
        log.info("Received M-Pesa callback: {}", callback);

        try {
            mpesaService.handleCallback(callback);
            return ResponseEntity.ok(Map.of(
                    "ResultCode", "0",
                    "ResultDesc", "Success"
            ));
        } catch (Exception e) {
            log.error("Error processing M-Pesa callback", e);
            return ResponseEntity.ok(Map.of(
                    "ResultCode", "1",
                    "ResultDesc", "Failed to process callback"
            ));
        }
    }

    @GetMapping("/mpesa/query/{checkoutRequestId}")
    @Operation(summary = "Query M-Pesa transaction status")
    public ResponseEntity<String> queryTransaction(@PathVariable String checkoutRequestId) {
        String result = mpesaService.queryTransaction(checkoutRequestId);
        return ResponseEntity.ok(result);
    }
}