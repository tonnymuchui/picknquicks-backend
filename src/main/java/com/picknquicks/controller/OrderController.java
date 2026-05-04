package com.picknquicks.controller;
import com.picknquicks.aspect.RateLimited;
import com.picknquicks.domain.order.OrderStatus;
import com.picknquicks.dto.request.order.CreateOrderRequest;
import com.picknquicks.dto.response.ApiResponse;
import com.picknquicks.dto.response.PaginatedResponse;
import com.picknquicks.dto.response.order.OrderResponse;
import com.picknquicks.service.order.OrderService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create new order", description = "Create order from cart (guest or authenticated)")
    @RateLimited
    @Timed(value = "order.create", description = "Time taken to create order")
    public ResponseEntity<ApiResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken
    ) {
        OrderResponse response = orderService.createOrder(request, guestToken);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", response));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    @Timed(value = "order.get", description = "Time taken to get order")
    public ResponseEntity<ApiResponse> getOrder(@PathVariable UUID orderId) {
        OrderResponse response = orderService.getOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order fetched successfully", response));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by order number")
    @Timed(value = "order.getByNumber", description = "Time taken to get order by number")
    public ResponseEntity<ApiResponse> getOrderByNumber(@PathVariable String orderNumber) {
        OrderResponse response = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success("Order fetched successfully", response));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get user orders")
    @Timed(value = "order.getUserOrders", description = "Time taken to get user orders")
    public ResponseEntity<ApiResponse> getUserOrders(
            @PathVariable UUID userId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PaginatedResponse<OrderResponse> response = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("User orders fetched successfully", response));
    }

    @GetMapping("/guest")
    @Operation(summary = "Get guest orders by email")
    @Timed(value = "order.getGuestOrders", description = "Time taken to get guest orders")
    public ResponseEntity<ApiResponse> getGuestOrders(
            @RequestParam String email,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PaginatedResponse<OrderResponse> response = orderService.getGuestOrders(email, pageable);
        return ResponseEntity.ok(ApiResponse.success("Guest orders fetched successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all orders (Admin only)")
    @Timed(value = "order.getAll", description = "Time taken to get all orders")
    public ResponseEntity<ApiResponse> getAllOrders(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PaginatedResponse<OrderResponse> response = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success("Orders fetched successfully", response));
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update order status (Admin only)")
    @Timed(value = "order.updateStatus", description = "Time taken to update order status")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestParam OrderStatus status
    ) {
        OrderResponse response = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", response));
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "Cancel order")
    @Timed(value = "order.cancel", description = "Time taken to cancel order")
    public ResponseEntity<ApiResponse> cancelOrder(
            @PathVariable UUID orderId,
            @RequestParam(required = false) String reason
    ) {
        OrderResponse response = orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", response));
    }
}