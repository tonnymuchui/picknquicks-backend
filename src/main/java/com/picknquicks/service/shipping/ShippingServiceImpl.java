package com.picknquicks.service.shipping;

import com.picknquicks.domain.shipping.ShippingLocation;
import com.picknquicks.domain.shipping.ShippingRate;
import com.picknquicks.dto.response.ShippingRateResponse;
import com.picknquicks.exception.BadRequestException;
import com.picknquicks.repository.shipping.ShippingLocationRepository;
import com.picknquicks.repository.shipping.ShippingRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingServiceImpl implements ShippingService {

    private final ShippingLocationRepository locationRepository;
    private final ShippingRateRepository rateRepository;

    private static final BigDecimal DEFAULT_SHIPPING_COST = new BigDecimal("300.00");

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "shippingCost", key = "#city + '-' + #orderAmount")
    public BigDecimal calculateShippingCost(String city, BigDecimal orderAmount) {
        ShippingLocation location = locationRepository.findByCity(city).orElse(null);

        if (location == null) {
            log.warn("Shipping location not found for city: {}. Using default rate.", city);
            return DEFAULT_SHIPPING_COST;
        }

        List<ShippingRate> applicableRates = rateRepository.findApplicableRates(
                location.getZone().getId(),
                orderAmount
        );

        if (applicableRates.isEmpty()) {
            log.warn("No applicable shipping rates for zone: {}. Using default rate.", location.getZone().getName());
            return DEFAULT_SHIPPING_COST;
        }

        ShippingRate rate = applicableRates.get(0);
        BigDecimal cost = rate.calculateCost(orderAmount);

        log.info("Calculated shipping cost for {}: KES {}", city, cost);

        return cost;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShippingRateResponse> getAvailableRates(String city, BigDecimal orderAmount) {
        ShippingLocation location = locationRepository.findByCity(city)
                .orElseThrow(() -> new BadRequestException("Shipping not available for city: " + city));

        List<ShippingRate> rates = rateRepository.findApplicableRates(
                location.getZone().getId(),
                orderAmount
        );

        return rates.stream()
                .map(this::toShippingRateResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getEstimatedDeliveryDays(String city) {
        ShippingLocation location = locationRepository.findByCity(city).orElse(null);

        if (location == null) {
            return 5;
        }

        List<ShippingRate> rates = rateRepository.findByZoneIdAndActiveTrue(location.getZone().getId());

        if (rates.isEmpty()) {
            return 5;
        }

        return rates.get(0).getEstimatedDaysMax();
    }

    private ShippingRateResponse toShippingRateResponse(ShippingRate rate) {
        return ShippingRateResponse.builder()
                .id(rate.getId())
                .name(rate.getName())
                .description(rate.getDescription())
                .cost(rate.getBaseCost())
                .estimatedDaysMin(rate.getEstimatedDaysMin())
                .estimatedDaysMax(rate.getEstimatedDaysMax())
                .build();
    }
}