package com.picknquicks.repository.shipping;
import com.picknquicks.domain.shipping.ShippingZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShippingZoneRepository extends JpaRepository<ShippingZone, UUID> {

    @Query("SELECT z FROM ShippingZone z WHERE z.active = true ORDER BY z.displayOrder ASC")
    List<ShippingZone> findAllActive();

    @Query("SELECT z FROM ShippingZone z LEFT JOIN FETCH z.locations WHERE z.id = :id")
    Optional<ShippingZone> findByIdWithLocations(@Param("id") UUID id);

    @Query("SELECT z FROM ShippingZone z LEFT JOIN FETCH z.rates WHERE z.id = :id")
    Optional<ShippingZone> findByIdWithRates(@Param("id") UUID id);
}