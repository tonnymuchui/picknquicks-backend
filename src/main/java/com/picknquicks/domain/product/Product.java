package com.picknquicks.domain.product;
import com.picknquicks.domain.brand.Brand;
import com.picknquicks.domain.category.Category;
import com.picknquicks.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_slug", columnList = "slug"),
                @Index(name = "idx_product_sku", columnList = "sku"),
                @Index(name = "idx_product_active", columnList = "active"),
                @Index(name = "idx_product_featured", columnList = "featured"),
                @Index(name = "idx_product_category", columnList = "category_id"),
                @Index(name = "idx_product_brand", columnList = "brand_id"),
                @Index(name = "idx_product_price", columnList = "price"),
                @Index(name = "idx_product_stock", columnList = "stock_quantity"),
                @Index(name = "idx_product_created", columnList = "created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_slug", columnNames = "slug"),
                @UniqueConstraint(name = "uk_product_sku", columnNames = "sku")
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String slug;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(length = 2000)
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "sale_price", precision = 10, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "low_stock_threshold")
    @Builder.Default
    private Integer lowStockThreshold = 10;

    @Column(name = "weight_grams")
    private Integer weightGrams;

    @Column(length = 50)
    private String dimensions;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean featured = false;

    @Column(name = "is_digital", nullable = false)
    @Builder.Default
    private Boolean isDigital = false;

    @Column(name = "requires_shipping", nullable = false)
    @Builder.Default
    private Boolean requiresShipping = true;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "sale_count", nullable = false)
    @Builder.Default
    private Long saleCount = 0L;

    @Column(name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "review_count", nullable = false)
    @Builder.Default
    private Long reviewCount = 0L;

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    @OrderBy("displayOrder ASC")
    private Set<ProductImage> images = new HashSet<>();

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<ProductVariant> variants = new HashSet<>();

    @Column(name = "meta_title", length = 128)
    private String metaTitle;

    @Column(name = "meta_description", length = 255)
    private String metaDescription;

    @Column(name = "meta_keywords", length = 255)
    private String metaKeywords;

    @Formula("CASE WHEN sale_price IS NOT NULL AND sale_price < price THEN ((price - sale_price) / price * 100) ELSE 0 END")
    private BigDecimal discountPercentage;

    public BigDecimal getEffectivePrice() {
        return salePrice != null && salePrice.compareTo(price) < 0 ? salePrice : price;
    }

    public boolean isOnSale() {
        return salePrice != null && salePrice.compareTo(price) < 0;
    }

    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    public boolean isLowStock() {
        return stockQuantity != null && lowStockThreshold != null &&
                stockQuantity > 0 && stockQuantity <= lowStockThreshold;
    }

    public boolean isOutOfStock() {
        return stockQuantity == null || stockQuantity <= 0;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementSaleCount() {
        this.saleCount++;
    }

    public void decrementStock(Integer quantity) {
        if (stockQuantity == null || stockQuantity < quantity) {
            throw new IllegalStateException("Insufficient stock");
        }
        this.stockQuantity -= quantity;
    }

    public void incrementStock(Integer quantity) {
        if (stockQuantity == null) {
            stockQuantity = 0;
        }
        this.stockQuantity += quantity;
    }

    public void updateRating(BigDecimal newRating, Long newReviewCount) {
        this.averageRating = newRating;
        this.reviewCount = newReviewCount;
    }

    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    public void removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
    }

    public ProductImage getPrimaryImage() {
        return images.stream()
                .filter(ProductImage::getIsPrimary)
                .findFirst()
                .orElse(images.stream().findFirst().orElse(null));
    }
}