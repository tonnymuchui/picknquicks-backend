package com.picknquicks.domain.order;

import com.picknquicks.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_addresses")
@Data
@EqualsAndHashCode(callSuper = true, exclude = "order")
@ToString(exclude = "order")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAddress extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "recipient_name", nullable = false, length = 255)
    private String recipientName;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String county;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(nullable = false, length = 100)
    @Builder.Default
    private String country = "Kenya";

    @Column(length = 500)
    private String notes;
}