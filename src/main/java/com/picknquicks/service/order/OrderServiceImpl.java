package com.picknquicks.service.order;

import com.picknquicks.domain.cart.Cart;
import com.picknquicks.domain.cart.CartItem;
import com.picknquicks.domain.cart.CartStatus;
import com.picknquicks.domain.order.*;
import com.picknquicks.domain.product.Product;
import com.picknquicks.domain.user.User;
import com.picknquicks.dto.request.order.CreateOrderRequest;
import com.picknquicks.dto.response.PaginatedResponse;
import com.picknquicks.dto.response.order.OrderResponse;
import com.picknquicks.event.OrderCreatedEvent;
import com.picknquicks.event.OrderPaidEvent;
import com.picknquicks.event.OrderStatusChangedEvent;
import com.picknquicks.exception.BadRequestException;
import com.picknquicks.exception.ResourceNotFoundException;
import com.picknquicks.mapper.OrderMapper;
import com.picknquicks.repository.cart.CartRepository;
import com.picknquicks.repository.order.OrderRepository;
import com.picknquicks.repository.order.PaymentRepository;
import com.picknquicks.repository.product.ProductRepository;
import com.picknquicks.repository.user.UserRepository;
import com.picknquicks.service.payment.MpesaService;
import com.picknquicks.service.shipping.ShippingService;
import com.picknquicks.util.PagingAndSortingHelper;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final ShippingService shippingService;
    private final MpesaService mpesaService;
    private final PagingAndSortingHelper pagingHelper;
    private final ApplicationEventPublisher eventPublisher;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    @Timed(value = "order.create", description = "Time taken to create order")
    public OrderResponse createOrder(CreateOrderRequest request, String guestToken) {
        Cart cart = findCart(guestToken);

        if (cart == null || !cart.hasItems()) {
            throw new BadRequestException("Cart is empty");
        }

        validateCartStock(cart);

        User user = null;
        if (guestToken == null) {
            UUID userId = getCurrentUserId();
            if (userId != null) {
                user = userRepository.findById(userId).orElse(null);
            }
        }

        BigDecimal subtotal = cart.getSubtotal();
        BigDecimal taxAmount = cart.getTax();
        BigDecimal shippingCost = shippingService.calculateShippingCost(
                request.getShippingAddress().getCity(),
                subtotal
        );
        BigDecimal totalAmount = subtotal.add(taxAmount).add(shippingCost);

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .customerName(request.getCustomerName())
                .status(OrderStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .shippingCost(shippingCost)
                .totalAmount(totalAmount)
                .notes(request.getNotes())
                .build();

        cart.getItems().forEach(cartItem -> {
            Product product = cartItem.getProduct();

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .productImageUrl(product.getPrimaryImage() != null ? product.getPrimaryImage().getImageUrl() : null)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getPrice())
                    .taxRate(cartItem.getTaxRate())
                    .build();

            order.addItem(orderItem);
        });

        OrderAddress shippingAddress = OrderAddress.builder()
                .recipientName(request.getShippingAddress().getRecipientName())
                .phoneNumber(request.getShippingAddress().getPhoneNumber())
                .addressLine1(request.getShippingAddress().getAddressLine1())
                .addressLine2(request.getShippingAddress().getAddressLine2())
                .city(request.getShippingAddress().getCity())
                .county(request.getShippingAddress().getCounty())
                .postalCode(request.getShippingAddress().getPostalCode())
                .country("Kenya")
                .notes(request.getShippingAddress().getNotes())
                .build();

        order.setShippingAddress(shippingAddress);

        BigDecimal amountToPay = order.getAmountToPay();

        Payment payment = Payment.builder()
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .amount(amountToPay)
                .phoneNumber(request.getPhoneNumber())
                .build();

        order.setPayment(payment);

        Integer estimatedDays = shippingService.getEstimatedDeliveryDays(request.getShippingAddress().getCity());
        order.setEstimatedDeliveryDate(LocalDateTime.now().plusDays(estimatedDays));

        Order savedOrder = orderRepository.save(order);

        if (request.getPaymentMethod() == PaymentMethod.MPESA) {
            try {
                mpesaService.initiateStkPush(savedOrder.getId(), request.getPhoneNumber(), amountToPay);
            } catch (Exception e) {
                log.error("Failed to initiate M-Pesa payment for order {}", savedOrder.getOrderNumber(), e);
            }
        }

        reserveStock(savedOrder);

        cart.setStatus(CartStatus.CONVERTED);
        cartRepository.save(cart);

        eventPublisher.publishEvent(new OrderCreatedEvent(this, savedOrder));

        log.info("Order created: {} for customer: {}", savedOrder.getOrderNumber(), savedOrder.getEmail());

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));

        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<OrderResponse> getUserOrders(UUID userId, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findByUserId(userId, pageable);
        return pagingHelper.toPaginatedResponse(orderPage, orderMapper::toOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<OrderResponse> getGuestOrders(String email, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findByEmail(email, pageable);
        return pagingHelper.toPaginatedResponse(orderPage, orderMapper::toOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAll(pageable);
        return pagingHelper.toPaginatedResponse(orderPage, orderMapper::toOrderResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderStatus oldStatus = order.getStatus();
        order.updateStatus(newStatus);

        Order savedOrder = orderRepository.save(order);

        eventPublisher.publishEvent(new OrderStatusChangedEvent(this, orderId, oldStatus, newStatus));

        log.info("Order {} status changed: {} -> {}", order.getOrderNumber(), oldStatus, newStatus);

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(UUID orderId, String reason) {
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.canBeCancelled()) {
            throw new BadRequestException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        OrderStatus oldStatus = order.getStatus();
        order.setCancellationReason(reason);
        order.updateStatus(OrderStatus.CANCELLED);

        restoreStock(order);

        Order savedOrder = orderRepository.save(order);

        eventPublisher.publishEvent(new OrderStatusChangedEvent(this, orderId, oldStatus, OrderStatus.CANCELLED));

        log.info("Order {} cancelled. Reason: {}", order.getOrderNumber(), reason);

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public void processPaymentSuccess(UUID orderId, String transactionId) {
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.updateStatus(OrderStatus.PAID);

        orderRepository.save(order);

        eventPublisher.publishEvent(new OrderPaidEvent(this, orderId, transactionId, order.getTotalAmount()));

        log.info("Payment processed successfully for order {}", order.getOrderNumber());
    }

    @Override
    @Transactional
    public void processPaymentFailure(UUID orderId, String reason) {
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setPaymentStatus(PaymentStatus.FAILED);
        order.updateStatus(OrderStatus.PAYMENT_FAILED);

        orderRepository.save(order);

        log.warn("Payment failed for order {}: {}", order.getOrderNumber(), reason);
    }

    private Cart findCart(String guestToken) {
        if (guestToken != null) {
            return cartRepository.findByGuestTokenAndStatusWithItems(guestToken, CartStatus.ACTIVE)
                    .orElse(null);
        }

        UUID userId = getCurrentUserId();
        if (userId != null) {
            return cartRepository.findByUserIdAndStatusWithItems(userId, CartStatus.ACTIVE)
                    .orElse(null);
        }

        return null;
    }

    private void validateCartStock(Cart cart) {
        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + cartItem.getProduct().getId()));

            if (!product.isInStock()) {
                throw new BadRequestException(product.getName() + " is out of stock");
            }

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException(
                        String.format("%s - Insufficient stock. Only %d available",
                                product.getName(), product.getStockQuantity())
                );
            }
        }
    }

    private void reserveStock(Order order) {
        for (OrderItem orderItem : order.getItems()) {
            Product product = productRepository.findByIdWithLock(orderItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            product.decrementStock(orderItem.getQuantity());
            productRepository.save(product);
        }

        log.info("Stock reserved for order {}", order.getOrderNumber());
    }

    private void restoreStock(Order order) {
        for (OrderItem orderItem : order.getItems()) {
            Product product = productRepository.findByIdWithLock(orderItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            product.incrementStock(orderItem.getQuantity());
            productRepository.save(product);
        }

        log.info("Stock restored for cancelled order {}", order.getOrderNumber());
    }

    private String generateOrderNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        int random = RANDOM.nextInt(9000) + 1000;
        return "ORD-" + timestamp.substring(timestamp.length() - 8) + "-" + random;
    }

    private UUID getCurrentUserId() {
        return null;
    }
}