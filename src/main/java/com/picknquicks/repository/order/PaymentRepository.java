package com.picknquicks.repository.order;
import com.picknquicks.domain.order.Payment;
import com.picknquicks.domain.order.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findByTransactionId(String transactionId);

    Optional<Payment> findByMpesaCheckoutRequestId(String checkoutRequestId);

    @Query("SELECT p FROM Payment p WHERE p.mpesaMerchantRequestId = :merchantRequestId")
    Optional<Payment> findByMpesaMerchantRequestId(@Param("merchantRequestId") String merchantRequestId);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    Long countByStatus(@Param("status") PaymentStatus status);
}