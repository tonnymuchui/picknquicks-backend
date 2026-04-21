package com.picknquicks.domain.product;

import com.picknquicks.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "product_variants",
        indexes = {
                @Index(name = "idx_product_variant_product", columnList = "product_id"),
                @Index(name = "idx_product_variant_sku", columnList = "sku")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_variant_sku", columnNames = "sku")
        }
)
@Data
@EqualsAndHashCode(callSuper = true, exclude = "product")
@ToString(exclude = "product")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "attribute_type", nullable = false, length = 50)
    private String attributeType;

    @Column(name = "attribute_value", nullable = false, length = 100)
    private String attributeValue;

    @Column(name = "price_adjustment", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal priceAdjustment = BigDecimal.ZERO;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    public BigDecimal getEffectivePrice(BigDecimal basePrice) {
        return basePrice.add(priceAdjustment);
    }

    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    public void decrementStock(Integer quantity) {
        if (stockQuantity == null || stockQuantity < quantity) {
            throw new IllegalStateException("Insufficient variant stock");
        }
        this.stockQuantity -= quantity;
    }
}