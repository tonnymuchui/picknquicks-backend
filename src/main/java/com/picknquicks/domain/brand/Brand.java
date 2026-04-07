package com.picknquicks.domain.brand;

import com.picknquicks.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "brands",
        indexes = {
                @Index(name = "idx_brand_slug", columnList = "slug"),
                @Index(name = "idx_brand_active", columnList = "active"),
                @Index(name = "idx_brand_featured", columnList = "featured"),
                @Index(name = "idx_brand_display_order", columnList = "display_order")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_brand_slug", columnNames = "slug"),
                @UniqueConstraint(name = "uk_brand_name", columnNames = "name")
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Brand extends BaseEntity {

    @Column(nullable = false, unique = true, length = 128)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String slug;

    @Column(length = 1000)
    private String description;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Column(name = "banner_url", length = 255)
    private String bannerUrl;

    @Column(name = "website_url", length = 255)
    private String websiteUrl;

    @Column(name = "country_of_origin", length = 64)
    private String countryOfOrigin;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean featured = false;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "product_count", nullable = false)
    @Builder.Default
    private Long productCount = 0L;

    @Column(name = "meta_title", length = 128)
    private String metaTitle;

    @Column(name = "meta_description", length = 255)
    private String metaDescription;

    @Column(name = "meta_keywords", length = 255)
    private String metaKeywords;

    public void incrementProductCount() {
        this.productCount++;
    }

    public void decrementProductCount() {
        if (this.productCount > 0) {
            this.productCount--;
        }
    }

    public boolean isFeatured() {
        return featured != null && featured && active;
    }

    public boolean hasProducts() {
        return productCount != null && productCount > 0;
    }
}