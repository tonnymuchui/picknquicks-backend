package com.picknquicks.mapper;
import com.picknquicks.domain.order.*;
import com.picknquicks.dto.response.*;
import com.picknquicks.dto.response.order.OrderAddressResponse;
import com.picknquicks.dto.response.order.OrderItemResponse;
import com.picknquicks.dto.response.order.OrderResponse;
import com.picknquicks.dto.response.order.PaymentResponse;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponse toOrderResponse(Order order) {
        if (order == null) {
            return null;
        }

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .email(order.getEmail())
                .phoneNumber(order.getPhoneNumber())
                .customerName(order.getCustomerName())
                .status(order.getStatus().name())
                .paymentMethod(order.getPaymentMethod().name())
                .paymentStatus(order.getPaymentStatus().name())
                .items(mapOrderItems(order.getItems()))
                .shippingAddress(mapAddress(order.getShippingAddress()))
                .payment(mapPayment(order.getPayment()))
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .shippingCost(order.getShippingCost())
                .totalAmount(order.getTotalAmount())
                .amountToPay(order.getAmountToPay())
                .notes(order.getNotes())
                .trackingNumber(order.getTrackingNumber())
                .estimatedDeliveryDate(order.getEstimatedDeliveryDate())
                .isGuest(order.isGuest())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private List<OrderItemResponse> mapOrderItems(java.util.Set<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        return items.stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList());
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProductName())
                .productSku(item.getProductSku())
                .productImageUrl(item.getProductImageUrl())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .taxRate(item.getTaxRate())
                .subtotal(item.getSubtotal())
                .taxAmount(item.getTaxAmount())
                .total(item.getTotal())
                .build();
    }

    private OrderAddressResponse mapAddress(OrderAddress address) {
        if (address == null) {
            return null;
        }

        return OrderAddressResponse.builder()
                .recipientName(address.getRecipientName())
                .phoneNumber(address.getPhoneNumber())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .county(address.getCounty())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .notes(address.getNotes())
                .build();
    }

    private PaymentResponse mapPayment(Payment payment) {
        if (payment == null) {
            return null;
        }

        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentMethod(payment.getPaymentMethod().name())
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .transactionId(payment.getTransactionId())
                .mpesaReceiptNumber(payment.getMpesaReceiptNumber())
                .phoneNumber(payment.getPhoneNumber())
                .paidAt(payment.getPaidAt())
                .failureReason(payment.getFailureReason())
                .build();
    }
}