package com.picknquicks.domain.order;

import com.picknquicks.domain.user.User;
import com.picknquicks.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_order_user", columnList = "user_id"),
                @Index(name = "idx_order_number", columnList = "order_number"),
                @Index(name = "idx_order_status", columnList = "status"),
                @Index(name = "idx_order_payment_status", columnList = "payment_status"),
                @Index(name = "idx_order_email", columnList = "email"),
                @Index(name = "idx_order_phone", columnList = "phone_number"),
                @Index(name = "idx_order_created", columnList = "created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_order_number", columnNames = "order_number")
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

    @Column(name = "order_number", nullable = false, unique = true, length = 20)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "customer_name", nullable = false, length = 255)
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<OrderItem> items = new HashSet<>();

    @OneToOne(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private OrderAddress shippingAddress;

    @OneToOne(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Payment payment;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "shipping_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingCost;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "admin_notes", length = 1000)
    private String adminNotes;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

     @Column(name = "cancellation_reason", length = 500)
     private String cancellationReason;

     public boolean isGuest() {
        return user == null;
    }

    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING ||
                status == OrderStatus.PAYMENT_PENDING ||
                status == OrderStatus.PAYMENT_FAILED;
    }

    public boolean canBeRefunded() {
        return status == OrderStatus.PAID ||
                status == OrderStatus.PROCESSING ||
                status == OrderStatus.SHIPPED;
    }

    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;

        if (newStatus == OrderStatus.DELIVERED) {
            this.deliveredAt = LocalDateTime.now();
        } else if (newStatus == OrderStatus.CANCELLED) {
            this.cancelledAt = LocalDateTime.now();
        }
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void setShippingAddress(OrderAddress address) {
        this.shippingAddress = address;
        address.setOrder(this);
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
        payment.setOrder(this);
    }

    public BigDecimal getAmountToPay() {
        if (paymentMethod == PaymentMethod.CASH_ON_DELIVERY) {
            return shippingCost;
        }
        return totalAmount;
    }
}