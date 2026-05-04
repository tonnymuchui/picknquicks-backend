package com.picknquicks.repository.shipping;
import com.picknquicks.domain.shipping.ShippingLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShippingLocationRepository extends JpaRepository<ShippingLocation, UUID> {

    @Query("SELECT sl FROM ShippingLocation sl WHERE LOWER(sl.city) = LOWER(:city)")
    Optional<ShippingLocation> findByCity(@Param("city") String city);

    @Query("SELECT sl FROM ShippingLocation sl WHERE LOWER(sl.county) = LOWER(:county)")
    Optional<ShippingLocation> findByCounty(@Param("county") String county);
}