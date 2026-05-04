package com.picknquicks.dto.response.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {
    private UUID id;
    private String paymentMethod;
    private String status;
    private BigDecimal amount;
    private String transactionId;
    private String mpesaReceiptNumber;
    private String phoneNumber;
    private LocalDateTime paidAt;
    private String failureReason;
}