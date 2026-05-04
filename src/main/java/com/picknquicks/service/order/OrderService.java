package com.picknquicks.service.order;
import com.picknquicks.domain.order.OrderStatus;
import com.picknquicks.dto.request.order.CreateOrderRequest;
import com.picknquicks.dto.response.PaginatedResponse;
import com.picknquicks.dto.response.order.OrderResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request, String guestToken);

    OrderResponse getOrder(UUID orderId);

    OrderResponse getOrderByNumber(String orderNumber);

    PaginatedResponse<OrderResponse> getUserOrders(UUID userId, Pageable pageable);

    PaginatedResponse<OrderResponse> getGuestOrders(String email, Pageable pageable);

    PaginatedResponse<OrderResponse> getAllOrders(Pageable pageable);

    OrderResponse updateOrderStatus(UUID orderId, OrderStatus newStatus);

    OrderResponse cancelOrder(UUID orderId, String reason);

    void processPaymentSuccess(UUID orderId, String transactionId);

    void processPaymentFailure(UUID orderId, String reason);
}