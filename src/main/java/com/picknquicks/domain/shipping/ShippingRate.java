package com.picknquicks.domain.shipping;
import com.picknquicks.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "shipping_rates",
        indexes = {
                @Index(name = "idx_shipping_rate_zone", columnList = "zone_id"),
                @Index(name = "idx_shipping_rate_active", columnList = "active")
        }
)
@Data
@EqualsAndHashCode(callSuper = true, exclude = "zone")
@ToString(exclude = "zone")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingRate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private ShippingZone zone;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "base_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseCost;

    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "max_order_amount", precision = 10, scale = 2)
    private BigDecimal maxOrderAmount;

    @Column(name = "free_shipping_threshold", precision = 10, scale = 2)
    private BigDecimal freeShippingThreshold;

    @Column(name = "estimated_days_min")
    private Integer estimatedDaysMin;

    @Column(name = "estimated_days_max")
    private Integer estimatedDaysMax;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    public BigDecimal calculateCost(BigDecimal orderAmount) {
        if (freeShippingThreshold != null &&
                orderAmount.compareTo(freeShippingThreshold) >= 0) {
            return BigDecimal.ZERO;
        }
        return baseCost;
    }
}