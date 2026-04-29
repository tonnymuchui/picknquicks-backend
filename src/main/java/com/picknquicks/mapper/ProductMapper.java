package com.picknquicks.mapper;

import com.picknquicks.domain.product.Product;
import com.picknquicks.domain.product.ProductImage;
import com.picknquicks.domain.product.ProductSearchDocument;
import com.picknquicks.dto.response.ProductResponse;
import com.picknquicks.dto.response.ProductImageResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public ProductResponse toProductResponse(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .price(product.getPrice())
                .salePrice(product.getSalePrice())
                .effectivePrice(product.getEffectivePrice())
                .discountPercentage(product.getDiscountPercentage())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .stockQuantity(product.getStockQuantity())
                .inStock(product.isInStock())
                .lowStock(product.isLowStock())
                .weightGrams(product.getWeightGrams())
                .dimensions(product.getDimensions())
                .active(product.getActive())
                .featured(product.getFeatured())
                .isDigital(product.getIsDigital())
                .requiresShipping(product.getRequiresShipping())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .saleCount(product.getSaleCount())
                .viewCount(product.getViewCount())
                .primaryImageUrl(convertImageUrl(product.getPrimaryImage() != null ? product.getPrimaryImage().getImageUrl() : null))
                .images(mapImages(product.getImages()))
                .metaTitle(product.getMetaTitle())
                .metaDescription(product.getMetaDescription())
                .metaKeywords(product.getMetaKeywords())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public ProductResponse toProductResponseWithoutRelations(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .price(product.getPrice())
                .salePrice(product.getSalePrice())
                .effectivePrice(product.getEffectivePrice())
                .stockQuantity(product.getStockQuantity())
                .inStock(product.isInStock())
                .active(product.getActive())
                .featured(product.getFeatured())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .primaryImageUrl(convertImageUrl(product.getPrimaryImage() != null ? product.getPrimaryImage().getImageUrl() : null))
                .createdAt(product.getCreatedAt())
                .build();
    }

    public List<ProductResponse> toResponseList(List<Product> products) {
        if (products == null) {
            return List.of();
        }

        return products.stream()
                .map(this::toProductResponseWithoutRelations)
                .collect(Collectors.toList());
    }

    public ProductSearchDocument toSearchDocument(Product product) {
        if (product == null) {
            return null;
        }

        return ProductSearchDocument.builder()
                .id(product.getId().toString())
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .price(product.getPrice())
                .salePrice(product.getSalePrice())
                .categoryId(product.getCategory() != null ? product.getCategory().getId().toString() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .brandId(product.getBrand() != null ? product.getBrand().getId().toString() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .stockQuantity(product.getStockQuantity())
                .active(product.getActive())
                .featured(product.getFeatured())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .saleCount(product.getSaleCount())
                .primaryImageUrl(convertImageUrl(product.getPrimaryImage() != null ? product.getPrimaryImage().getImageUrl() : null))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private List<ProductImageResponse> mapImages(java.util.Set<ProductImage> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        return images.stream()
                .map(img -> ProductImageResponse.builder()
                        .id(img.getId())
                        .imageUrl(convertImageUrl(img.getImageUrl()))
                        .altText(img.getAltText())
                        .isPrimary(img.getIsPrimary())
                        .displayOrder(img.getDisplayOrder())
                        .build())
                .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                .collect(Collectors.toList());
    }

    private String convertImageUrl(String relativeImageUrl) {
        if (relativeImageUrl == null || relativeImageUrl.isBlank()) {
            return null;
        }
        // Convert relative path like "products/uuid.jpg" to "/api/files/preview/products/uuid.jpg"
        return "/api/files/preview/" + relativeImageUrl;
    }
}