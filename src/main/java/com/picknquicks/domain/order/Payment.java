package com.picknquicks.domain.order;
import com.picknquicks.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payment_order", columnList = "order_id"),
                @Index(name = "idx_payment_transaction_id", columnList = "transaction_id"),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_method", columnList = "payment_method")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_transaction_id", columnNames = "transaction_id")
        }
)
@Data
@EqualsAndHashCode(callSuper = true, exclude = "order")
@ToString(exclude = "order")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_id", unique = true, length = 100)
    private String transactionId;

    @Column(name = "mpesa_checkout_request_id", length = 100)
    private String mpesaCheckoutRequestId;

    @Column(name = "mpesa_merchant_request_id", length = 100)
    private String mpesaMerchantRequestId;

    @Column(name = "mpesa_receipt_number", length = 100)
    private String mpesaReceiptNumber;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "callback_data", columnDefinition = "TEXT")
    private String callbackData;


    public boolean isCompleted() {
        return status == PaymentStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }

    public void markAsCompleted(String transactionId, String receiptNumber) {
        this.status = PaymentStatus.COMPLETED;
        this.transactionId = transactionId;
        this.mpesaReceiptNumber = receiptNumber;
        this.paidAt = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }
}