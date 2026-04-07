package com.picknquicks.mapper;
import com.picknquicks.domain.brand.Brand;
import com.picknquicks.dto.response.BrandResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BrandMapper {

    public BrandResponse toBrandResponse(Brand brand) {
        if (brand == null) {
            return null;
        }

        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .slug(brand.getSlug())
                .description(brand.getDescription())
                .logoUrl(brand.getLogoUrl())
                .bannerUrl(brand.getBannerUrl())
                .websiteUrl(brand.getWebsiteUrl())
                .countryOfOrigin(brand.getCountryOfOrigin())
                .active(brand.getActive())
                .featured(brand.getFeatured())
                .displayOrder(brand.getDisplayOrder())
                .productCount(brand.getProductCount())
                .metaTitle(brand.getMetaTitle())
                .metaDescription(brand.getMetaDescription())
                .metaKeywords(brand.getMetaKeywords())
                .createdAt(brand.getCreatedAt())
                .updatedAt(brand.getUpdatedAt())
                .build();
    }

    public List<BrandResponse> toResponseList(List<Brand> brands) {
        if (brands == null) {
            return List.of();
        }

        return brands.stream()
                .map(this::toBrandResponse)
                .collect(Collectors.toList());
    }
}