package com.picknquicks.domain.shipping;

import com.picknquicks.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "shipping_zones",
        indexes = {
                @Index(name = "idx_shipping_zone_name", columnList = "name"),
                @Index(name = "idx_shipping_zone_active", columnList = "active")
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingZone extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @OneToMany(
            mappedBy = "zone",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<ShippingLocation> locations = new HashSet<>();

    @OneToMany(
            mappedBy = "zone",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<ShippingRate> rates = new HashSet<>();
}