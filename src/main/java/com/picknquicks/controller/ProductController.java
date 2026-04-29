package com.picknquicks.controller;
import com.picknquicks.aspect.RateLimited;
import com.picknquicks.dto.request.CreateProductRequest;
import com.picknquicks.dto.request.UpdateProductRequest;
import com.picknquicks.dto.request.UpdateStockRequest;
import com.picknquicks.dto.response.ApiResponse;
import com.picknquicks.dto.response.PaginatedResponse;
import com.picknquicks.dto.response.ProductResponse;
import com.picknquicks.service.IdempotencyService;
import com.picknquicks.service.product.ProductService;
import com.picknquicks.util.PagingAndSortingHelper;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

    private final ProductService productService;
    private final PagingAndSortingHelper pagingHelper;
    private final IdempotencyService idempotencyService;

    @GetMapping
    @Operation(summary = "Get all products")
    @Timed(value = "products.get.all", description = "Time taken to get all products")
    public ResponseEntity<ApiResponse> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Pageable pageable = pagingHelper.buildPageable(page, size, sortBy, sortDirection);
        PaginatedResponse<ProductResponse> response = productService.getAllProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success("Products fetched successfully", response));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active products")
    @Timed(value = "products.get.active")
    public ResponseEntity<ApiResponse> getActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        Pageable pageable = pagingHelper.buildPageable(page, size, sortBy, sortDirection);
        PaginatedResponse<ProductResponse> response = productService.getActiveProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success("Active products fetched successfully", response));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured products")
    @Timed(value = "products.get.featured")
    public ResponseEntity<ApiResponse> getFeaturedProducts() {
        List<ProductResponse> response = productService.getFeaturedProducts();
        return ResponseEntity.ok(ApiResponse.success("Featured products fetched successfully", response));
    }

    @GetMapping("/on-sale")
    @Operation(summary = "Get products on sale")
    public ResponseEntity<ApiResponse> getOnSaleProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = pagingHelper.buildPageable(page, size, "discountPercentage", "DESC");
        PaginatedResponse<ProductResponse> response = productService.getOnSaleProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success("Sale products fetched successfully", response));
    }

    @GetMapping("/best-sellers")
    @Operation(summary = "Get best selling products")
    @Timed(value = "products.get.bestsellers")
    public ResponseEntity<ApiResponse> getBestSellers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = pagingHelper.buildPageable(page, size, "saleCount", "DESC");
        PaginatedResponse<ProductResponse> response = productService.getBestSellers(pageable);
        return ResponseEntity.ok(ApiResponse.success("Best sellers fetched successfully", response));
    }

    @GetMapping("/new-arrivals")
    @Operation(summary = "Get new arrival products")
    public ResponseEntity<ApiResponse> getNewArrivals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = pagingHelper.buildPageable(page, size, "createdAt", "DESC");
        PaginatedResponse<ProductResponse> response = productService.getNewArrivals(pageable);
        return ResponseEntity.ok(ApiResponse.success("New arrivals fetched successfully", response));
    }

    @GetMapping("/top-rated")
    @Operation(summary = "Get top rated products")
    public ResponseEntity<ApiResponse> getTopRated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = pagingHelper.buildPageable(page, size, "averageRating", "DESC");
        PaginatedResponse<ProductResponse> response = productService.getTopRated(pageable);
        return ResponseEntity.ok(ApiResponse.success("Top rated products fetched successfully", response));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID")
    @Timed(value = "products.get.byId")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable UUID productId) {
        ProductResponse response = productService.getProductById(productId);
        productService.incrementViewCount(productId);
        return ResponseEntity.ok(ApiResponse.success("Product fetched successfully", response));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get product by slug")
    @Timed(value = "products.get.bySlug")
    public ResponseEntity<ApiResponse> getProductBySlug(@PathVariable String slug) {
        ProductResponse response = productService.getProductBySlug(slug);
        productService.incrementViewCount(response.getId());
        return ResponseEntity.ok(ApiResponse.success("Product fetched successfully", response));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<ApiResponse> getProductsByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        Pageable pageable = pagingHelper.buildPageable(page, size, sortBy, sortDirection);
        PaginatedResponse<ProductResponse> response = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Products fetched successfully", response));
    }

    @GetMapping("/brand/{brandId}")
    @Operation(summary = "Get products by brand")
    public ResponseEntity<ApiResponse> getProductsByBrand(
            @PathVariable UUID brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        Pageable pageable = pagingHelper.buildPageable(page, size, sortBy, sortDirection);
        PaginatedResponse<ProductResponse> response = productService.getProductsByBrand(brandId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Products fetched successfully", response));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products")
    @RateLimited
    @Timed(value = "products.search")
    public ResponseEntity<ApiResponse> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        Pageable pageable = pagingHelper.buildPageable(page, size, sortBy, sortDirection);
        PaginatedResponse<ProductResponse> response = productService.searchProducts(query, pageable);
        return ResponseEntity.ok(ApiResponse.success("Products search completed successfully", response));
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter products")
    public ResponseEntity<ApiResponse> filterProducts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        Pageable pageable = pagingHelper.buildPageable(page, size, sortBy, sortDirection);
        PaginatedResponse<ProductResponse> response = productService.filterProducts(
                categoryId, brandId, minPrice, maxPrice, pageable
        );
        return ResponseEntity.ok(ApiResponse.success("Products filtered successfully", response));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create product")
    @Timed(value = "products.create")
    public ResponseEntity<ApiResponse> createProduct(
            @Valid @ModelAttribute CreateProductRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        if (idempotencyKey != null) {
            Optional<com.picknquicks.util.IdempotencyKey> existing = idempotencyService.findByKey(idempotencyKey);
            if (existing.isPresent()) {
                return ResponseEntity.status(existing.get().getResponseStatus())
                        .body(ApiResponse.success("Product already created (idempotent)", null));
            }
        }

        ProductResponse response = productService.createProduct(request);

        if (idempotencyKey != null) {
            idempotencyService.save(idempotencyKey, "", response.toString(), 200);
        }

        return ResponseEntity.ok(ApiResponse.success("Product created successfully", response));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create product (JSON)")
    public ResponseEntity<ApiResponse> createProductJson(
            @Valid @RequestBody CreateProductRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        if (idempotencyKey != null) {
            Optional<com.picknquicks.util.IdempotencyKey> existing = idempotencyService.findByKey(idempotencyKey);
            if (existing.isPresent()) {
                return ResponseEntity.status(existing.get().getResponseStatus())
                        .body(ApiResponse.success("Product already created (idempotent)", null));
            }
        }

        ProductResponse response = productService.createProduct(request);

        if (idempotencyKey != null) {
            idempotencyService.save(idempotencyKey, "", response.toString(), 200);
        }

        return ResponseEntity.ok(ApiResponse.success("Product created successfully", response));
    }

    @PutMapping(value = "/{productId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update product")
    @Timed(value = "products.update")
    public ResponseEntity<ApiResponse> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        ProductResponse response = productService.updateProduct(productId, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", response));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete product")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable UUID productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
    }

    @PostMapping(value = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Add product image")
    public ResponseEntity<ApiResponse> addProductImage(
            @PathVariable UUID productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String altText,
            @RequestParam(required = false) Boolean isPrimary
    ) {
        ProductResponse response = productService.addProductImage(productId, file, altText, isPrimary);
        return ResponseEntity.ok(ApiResponse.success("Product image added successfully", response));
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Remove product image")
    public ResponseEntity<ApiResponse> removeProductImage(
            @PathVariable UUID productId,
            @PathVariable UUID imageId
    ) {
        productService.removeProductImage(productId, imageId);
        return ResponseEntity.ok(ApiResponse.success("Product image removed successfully"));
    }

    @PatchMapping("/{productId}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update product stock")
    @Timed(value = "products.stock.update")
    public ResponseEntity<ApiResponse> updateStock(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateStockRequest request
    ) {
        ProductResponse response = productService.updateStock(productId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Stock updated successfully", response));
    }

    @GetMapping("/inventory/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get low stock products")
    public ResponseEntity<ApiResponse> getLowStockProducts() {
        List<ProductResponse> response = productService.getLowStockProducts();
        return ResponseEntity.ok(ApiResponse.success("Low stock products fetched successfully", response));
    }

    @GetMapping("/inventory/out-of-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get out of stock products")
    public ResponseEntity<ApiResponse> getOutOfStockProducts() {
        List<ProductResponse> response = productService.getOutOfStockProducts();
        return ResponseEntity.ok(ApiResponse.success("Out of stock products fetched successfully", response));
    }
}