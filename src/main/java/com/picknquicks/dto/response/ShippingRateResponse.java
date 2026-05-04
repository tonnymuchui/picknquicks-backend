package com.picknquicks.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingRateResponse {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal cost;
    private Integer estimatedDaysMin;
    private Integer estimatedDaysMax;
}