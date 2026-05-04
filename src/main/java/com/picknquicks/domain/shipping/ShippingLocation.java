package com.picknquicks.domain.shipping;
import com.picknquicks.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "shipping_locations",
        indexes = {
                @Index(name = "idx_shipping_location_zone", columnList = "zone_id"),
                @Index(name = "idx_shipping_location_city", columnList = "city")
        }
)
@Data
@EqualsAndHashCode(callSuper = true, exclude = "zone")
@ToString(exclude = "zone")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingLocation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private ShippingZone zone;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String county;

    @Column(name = "postal_code", length = 20)
    private String postalCode;
}