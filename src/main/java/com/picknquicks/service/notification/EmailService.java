package com.picknquicks.service.notification;

import com.picknquicks.domain.order.Order;
import com.picknquicks.domain.user.User;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final Resend resend;
    private final TemplateEngine templateEngine;

    @Value("${resend.from-email}")
    private String fromEmail;

    @Value("${resend.from-name}")
    private String fromName;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Async
    public void sendVerificationEmail(User user, String token) {
        String verificationUrl = frontendUrl + "/auth/verify-email?token=" + token;

        Context context = new Context();
        context.setVariable("firstName", user.getFirstName());
        context.setVariable("verificationUrl", verificationUrl);

        String htmlBody = templateEngine.process("email/verification", context);
        sendEmail(user.getEmail(), "Verify Your Email - PickNQuicks", htmlBody);
    }

    @Async
    public void sendPasswordResetEmail(User user, String token) {
        String resetUrl = frontendUrl + "/auth/reset-password?token=" + token;

        Context context = new Context();
        context.setVariable("firstName", user.getFirstName());
        context.setVariable("resetUrl", resetUrl);

        String htmlBody = templateEngine.process("email/password-reset", context);
        sendEmail(user.getEmail(), "Reset Your Password - PickNQuicks", htmlBody);
    }

    @Async
    public void sendWelcomeEmail(User user) {
        Context context = new Context();
        context.setVariable("firstName", user.getFirstName());
        context.setVariable("frontendUrl", frontendUrl);

        String htmlBody = templateEngine.process("email/welcome", context);
        sendEmail(user.getEmail(), "Welcome to PickNQuicks", htmlBody);
    }

     @Async
     public void sendStaffWelcomeEmail(User user, String temporaryPassword) {
         String loginUrl = frontendUrl + "/auth/login";

         Context context = new Context();
         context.setVariable("firstName", user.getFirstName());
         context.setVariable("temporaryPassword", temporaryPassword);
         context.setVariable("loginUrl", loginUrl);

         String htmlBody = templateEngine.process("email/staff-welcome", context);
         sendEmail(user.getEmail(), "Welcome to PickNQuicks Team", htmlBody);
     }

     @Async
     @CircuitBreaker(name = "emailService", fallbackMethod = "emailFallback")
     public void sendOrderConfirmationEmail(Order order) {
         Map<String, Object> variables = new HashMap<>();
         variables.put("customerName", order.getCustomerName());
         variables.put("orderNumber", order.getOrderNumber());
         variables.put("orderDate", order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
         variables.put("items", order.getItems());
         variables.put("subtotal", order.getSubtotal());
         variables.put("tax", order.getTaxAmount());
         variables.put("shippingCost", order.getShippingCost());
         variables.put("total", order.getTotalAmount());
         variables.put("shippingAddress", order.getShippingAddress());
         variables.put("paymentMethod", order.getPaymentMethod().name());

         sendTemplateEmail(
             order.getEmail(),
             "Order Confirmation - " + order.getOrderNumber(),
             "email/order-confirmation",
             variables
         );

         log.info("Order confirmation email sent to {} for order {}", order.getEmail(), order.getOrderNumber());
     }

     @Async
     @CircuitBreaker(name = "emailService", fallbackMethod = "emailFallback")
     public void sendPaymentConfirmationEmail(Order order) {
         Map<String, Object> variables = new HashMap<>();
         variables.put("customerName", order.getCustomerName());
         variables.put("orderNumber", order.getOrderNumber());
         variables.put("amount", order.getTotalAmount());
         variables.put("paymentMethod", order.getPaymentMethod().name());
         variables.put("transactionId", order.getPayment() != null ? order.getPayment().getTransactionId() : "N/A");
         variables.put("paidAt", order.getPayment() != null && order.getPayment().getPaidAt() != null
             ? order.getPayment().getPaidAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
             : "N/A");

         sendTemplateEmail(
             order.getEmail(),
             "Payment Confirmed - " + order.getOrderNumber(),
             "email/payment-confirmation",
             variables
         );

         log.info("Payment confirmation email sent to {} for order {}", order.getEmail(), order.getOrderNumber());
     }

     @Async
     @CircuitBreaker(name = "emailService", fallbackMethod = "emailFallback")
     public void sendOrderShippedEmail(Order order) {
         Map<String, Object> variables = new HashMap<>();
         variables.put("customerName", order.getCustomerName());
         variables.put("orderNumber", order.getOrderNumber());
         variables.put("trackingNumber", order.getTrackingNumber());
         variables.put("estimatedDelivery", order.getEstimatedDeliveryDate() != null
             ? order.getEstimatedDeliveryDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
             : "3-5 business days");

         sendTemplateEmail(
             order.getEmail(),
             "Your Order Has Been Shipped - " + order.getOrderNumber(),
             "email/order-shipped",
             variables
         );

         log.info("Order shipped email sent to {} for order {}", order.getEmail(), order.getOrderNumber());
     }

     @Async
     @CircuitBreaker(name = "emailService", fallbackMethod = "emailFallback")
     public void sendOrderDeliveredEmail(Order order) {
         Map<String, Object> variables = new HashMap<>();
         variables.put("customerName", order.getCustomerName());
         variables.put("orderNumber", order.getOrderNumber());

         sendTemplateEmail(
             order.getEmail(),
             "Order Delivered - " + order.getOrderNumber(),
             "email/order-delivered",
             variables
         );

         log.info("Order delivered email sent to {} for order {}", order.getEmail(), order.getOrderNumber());
     }

     @Async
     @CircuitBreaker(name = "emailService", fallbackMethod = "emailFallback")
     public void sendOrderCancelledEmail(Order order) {
         Map<String, Object> variables = new HashMap<>();
         variables.put("customerName", order.getCustomerName());
         variables.put("orderNumber", order.getOrderNumber());
         variables.put("cancellationReason", order.getCancellationReason());

         sendTemplateEmail(
             order.getEmail(),
             "Order Cancelled - " + order.getOrderNumber(),
             "email/order-cancelled",
             variables
         );

         log.info("Order cancelled email sent to {} for order {}", order.getEmail(), order.getOrderNumber());
     }

     @Async
     @CircuitBreaker(name = "emailService", fallbackMethod = "emailFallback")
     public void sendCartAbandonmentEmail(String email, String cartUrl) {
         Map<String, Object> variables = new HashMap<>();
         variables.put("cartUrl", cartUrl);

         sendTemplateEmail(
             email,
             "You Left Items in Your Cart",
             "email/cart-abandonment",
             variables
         );

         log.info("Cart abandonment email sent to {}", email);
     }

     private void sendEmail(String to, String subject, String htmlBody) {
         try {
             CreateEmailOptions request = CreateEmailOptions.builder()
                     .from(fromName + " <" + fromEmail + ">")
                     .to(to)
                     .subject(subject)
                     .html(htmlBody)
                     .build();

             CreateEmailResponse response = resend.emails().send(request);
             log.info("Email sent successfully to {} - ID: {}", to, response.getId());

         } catch (ResendException e) {
             log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
         }
     }

     private void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) {
         try {
             Context context = new Context();
             context.setVariables(variables);

             String htmlBody = templateEngine.process(templateName, context);
             sendEmail(to, subject, htmlBody);

         } catch (Exception e) {
             log.error("Failed to process template {} for email to {}: {}", templateName, to, e.getMessage(), e);
         }
     }

     private void emailFallback(Order order, Exception ex) {
         log.error("Email service circuit breaker fallback triggered for order {}: {}",
             order != null ? order.getOrderNumber() : "unknown", ex.getMessage(), ex);
     }

     private void emailFallback(String email, String cartUrl, Exception ex) {
         log.error("Email service circuit breaker fallback triggered for email {}: {}", email, ex.getMessage(), ex);
     }
 }
