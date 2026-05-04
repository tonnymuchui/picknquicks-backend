package com.picknquicks.repository.shipping;
import com.picknquicks.domain.shipping.ShippingRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShippingRateRepository extends JpaRepository<ShippingRate, UUID> {

    @Query("SELECT sr FROM ShippingRate sr WHERE sr.zone.id = :zoneId AND sr.active = true")
    List<ShippingRate> findByZoneIdAndActiveTrue(@Param("zoneId") UUID zoneId);

    @Query("SELECT sr FROM ShippingRate sr WHERE sr.zone.id = :zoneId AND sr.active = true " +
            "AND (sr.minOrderAmount IS NULL OR sr.minOrderAmount <= :orderAmount) " +
            "AND (sr.maxOrderAmount IS NULL OR sr.maxOrderAmount >= :orderAmount)")
    List<ShippingRate> findApplicableRates(@Param("zoneId") UUID zoneId, @Param("orderAmount") BigDecimal orderAmount);
}