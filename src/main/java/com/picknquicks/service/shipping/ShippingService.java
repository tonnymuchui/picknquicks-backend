package com.picknquicks.service.shipping;
import com.picknquicks.dto.response.ShippingRateResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ShippingService {

    BigDecimal calculateShippingCost(String city, BigDecimal orderAmount);

    List<ShippingRateResponse> getAvailableRates(String city, BigDecimal orderAmount);

    Integer getEstimatedDeliveryDays(String city);
}