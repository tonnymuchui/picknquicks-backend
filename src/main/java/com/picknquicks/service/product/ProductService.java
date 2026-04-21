package com.picknquicks.service.product;

import com.picknquicks.dto.request.CreateProductRequest;
import com.picknquicks.dto.request.UpdateProductRequest;
import com.picknquicks.dto.response.PaginatedResponse;
import com.picknquicks.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse updateProduct(UUID productId, UpdateProductRequest request);

    void deleteProduct(UUID productId);

    ProductResponse getProductById(UUID productId);

    ProductResponse getProductBySlug(String slug);

    PaginatedResponse<ProductResponse> getAllProducts(Pageable pageable);

    PaginatedResponse<ProductResponse> getActiveProducts(Pageable pageable);

    List<ProductResponse> getFeaturedProducts();

    PaginatedResponse<ProductResponse> getProductsByCategory(UUID categoryId, Pageable pageable);

    PaginatedResponse<ProductResponse> getProductsByBrand(UUID brandId, Pageable pageable);

    PaginatedResponse<ProductResponse> getOnSaleProducts(Pageable pageable);

    PaginatedResponse<ProductResponse> getBestSellers(Pageable pageable);

    PaginatedResponse<ProductResponse> getNewArrivals(Pageable pageable);

    PaginatedResponse<ProductResponse> getTopRated(Pageable pageable);

    PaginatedResponse<ProductResponse> searchProducts(String query, Pageable pageable);

    PaginatedResponse<ProductResponse> filterProducts(UUID categoryId, UUID brandId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    ProductResponse addProductImage(UUID productId, MultipartFile imageFile, String altText, Boolean isPrimary);

    void removeProductImage(UUID productId, UUID imageId);

    ProductResponse updateStock(UUID productId, Integer quantity);

    void incrementViewCount(UUID productId);

    List<ProductResponse> getLowStockProducts();

    List<ProductResponse> getOutOfStockProducts();
}