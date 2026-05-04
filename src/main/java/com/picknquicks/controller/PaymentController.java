package com.picknquicks.controller;
import com.picknquicks.dto.mpesa.MpesaCallbackRequest;
import com.picknquicks.service.payment.MpesaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment processing endpoints")
public class PaymentController {

    private final MpesaService mpesaService;

    @PostMapping("/mpesa/callback")
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