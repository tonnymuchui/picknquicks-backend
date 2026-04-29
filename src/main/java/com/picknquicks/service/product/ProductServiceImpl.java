package com.picknquicks.service.product;

import com.picknquicks.domain.brand.Brand;
import com.picknquicks.domain.category.Category;
import com.picknquicks.domain.product.Product;
import com.picknquicks.domain.product.ProductImage;
import com.picknquicks.dto.request.CreateProductRequest;
import com.picknquicks.dto.request.UpdateProductRequest;
import com.picknquicks.dto.response.PaginatedResponse;
import com.picknquicks.dto.response.ProductResponse;
import com.picknquicks.event.ProductCreatedEvent;
import com.picknquicks.event.ProductStockChangedEvent;
import com.picknquicks.exception.BadRequestException;
import com.picknquicks.exception.ResourceNotFoundException;
import com.picknquicks.mapper.ProductMapper;
import com.picknquicks.repository.brand.BrandRepository;
import com.picknquicks.repository.category.CategoryRepository;
import com.picknquicks.repository.product.ProductRepository;
import com.picknquicks.repository.product.ProductSearchRepository;
import com.picknquicks.service.storage.FileStorageService;
import com.picknquicks.util.PagingAndSortingHelper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductSearchRepository searchRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductMapper productMapper;
    private final PagingAndSortingHelper pagingHelper;
    private final FileStorageService fileStorageService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    @CacheEvict(value = {"products", "featuredProducts"}, allEntries = true)
    public ProductResponse createProduct(CreateProductRequest request) {
        validateUniqueness(request.getSlug(), request.getSku(), null);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Brand brand = null;
        if (request.getBrandId() != null) {
            brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        }

        Product product = Product.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .sku(request.getSku())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .price(request.getPrice())
                .salePrice(request.getSalePrice())
                .costPrice(request.getCostPrice())
                .taxRate(request.getTaxRate())
                .category(category)
                .brand(brand)
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                .lowStockThreshold(request.getLowStockThreshold())
                .weightGrams(request.getWeightGrams())
                .dimensions(request.getDimensions())
                .active(request.getActive() != null ? request.getActive() : true)
                .featured(request.getFeatured() != null ? request.getFeatured() : false)
                .isDigital(request.getIsDigital() != null ? request.getIsDigital() : false)
                .requiresShipping(request.getRequiresShipping() != null ? request.getRequiresShipping() : true)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .metaKeywords(request.getMetaKeywords())
                .build();

        Product savedProduct = productRepository.save(product);

        if (brand != null) {
            brand.incrementProductCount();
            brandRepository.save(brand);
        }

        searchRepository.save(productMapper.toSearchDocument(savedProduct));

        eventPublisher.publishEvent(new ProductCreatedEvent(this, savedProduct));

        log.info("Created product: {} with ID: {}", savedProduct.getName(), savedProduct.getId());

        return productMapper.toProductResponse(savedProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "product", "featuredProducts"}, allEntries = true)
    public ProductResponse updateProduct(UUID productId, UpdateProductRequest request) {
        Product product = findProductOrThrow(productId);

        if (request.getSlug() != null && !request.getSlug().equals(product.getSlug())) {
            validateSlugUniqueness(request.getSlug(), productId);
            product.setSlug(request.getSlug());
        }

        if (request.getSku() != null && !request.getSku().equals(product.getSku())) {
            validateSkuUniqueness(request.getSku(), productId);
            product.setSku(request.getSku());
        }

        updateIfPresent(request.getName(), product::setName);
        updateIfPresent(request.getDescription(), product::setDescription);
        updateIfPresent(request.getShortDescription(), product::setShortDescription);
        updateIfPresent(request.getPrice(), product::setPrice);
        updateIfPresent(request.getSalePrice(), product::setSalePrice);
        updateIfPresent(request.getCostPrice(), product::setCostPrice);
        updateIfPresent(request.getTaxRate(), product::setTaxRate);
        updateIfPresent(request.getLowStockThreshold(), product::setLowStockThreshold);
        updateIfPresent(request.getWeightGrams(), product::setWeightGrams);
        updateIfPresent(request.getDimensions(), product::setDimensions);
        updateIfPresent(request.getActive(), product::setActive);
        updateIfPresent(request.getFeatured(), product::setFeatured);
        updateIfPresent(request.getIsDigital(), product::setIsDigital);
        updateIfPresent(request.getRequiresShipping(), product::setRequiresShipping);
        updateIfPresent(request.getDisplayOrder(), product::setDisplayOrder);
        updateIfPresent(request.getMetaTitle(), product::setMetaTitle);
        updateIfPresent(request.getMetaDescription(), product::setMetaDescription);
        updateIfPresent(request.getMetaKeywords(), product::setMetaKeywords);

        if (request.getCategoryId() != null && !request.getCategoryId().equals(product.getCategory().getId())) {
            Category newCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(newCategory);
        }

        if (request.getBrandId() != null) {
            Brand oldBrand = product.getBrand();
            Brand newBrand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));

            if (oldBrand != null && !oldBrand.getId().equals(newBrand.getId())) {
                oldBrand.decrementProductCount();
                brandRepository.save(oldBrand);
            }

            if (oldBrand == null || !oldBrand.getId().equals(newBrand.getId())) {
                newBrand.incrementProductCount();
                brandRepository.save(newBrand);
            }

            product.setBrand(newBrand);
        }

        Product updatedProduct = productRepository.save(product);
        searchRepository.save(productMapper.toSearchDocument(updatedProduct));

        log.info("Updated product with ID: {}", productId);

        return productMapper.toProductResponse(updatedProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "product", "featuredProducts"}, allEntries = true)
    public void deleteProduct(UUID productId) {
        Product product = findProductOrThrow(productId);

        product.getImages().forEach(image -> fileStorageService.deleteFile(image.getImageUrl()));

        if (product.getBrand() != null) {
            product.getBrand().decrementProductCount();
            brandRepository.save(product.getBrand());
        }

        searchRepository.deleteById(productId.toString());
        productRepository.delete(product);

        log.info("Deleted product with ID: {}", productId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "product", key = "#productId")
    public ProductResponse getProductById(UUID productId) {
        Product product = findProductOrThrow(productId);
        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "product", key = "#slug")
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductBySlugFallback")
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products")
    public PaginatedResponse<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);
        return pagingHelper.toPaginatedResponse(productPage, productMapper::toProductResponseWithoutRelations);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'active-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public PaginatedResponse<ProductResponse> getActiveProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAllActive(pageable);
        return pagingHelper.toPaginatedResponse(productPage, productMapper::toProductResponseWithoutRelations);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "featuredProducts")
    public List<ProductResponse> getFeaturedProducts() {
        List<Product> products = productRepository.findFeaturedProducts();
        return productMapper.toResponseList(products);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ProductResponse> getProductsByCategory(UUID categoryId, Pageable pageable) {
        Page<Product> productPage = productRepository.findByCategoryId(categoryId, pageable);
        return pagingHelper.toPaginatedResponse(productPage, productMapper::toProductResponseWithoutRelations);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ProductResponse> getProductsByBrand(UUID brandId, Pageable pageable) {
        Page<Product> productPage = productRepository.findByBrandId(brandId, pageable);
        return pagingHelper.toPaginatedResponse(productPage, productMapper::toProductResponseWithoutRelations);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'on-sale-' + #pageable.pageNumber")
    public PaginatedResponse<ProductResponse> getOnSaleProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findOnSaleProducts(pageable);
        return pagingHelper.toPaginatedResponse(productPage, productMapper::toProductResponseWithoutRelations);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'best-sellers-' + #pageable.pageNumber")
    public PaginatedResponse<ProductResponse> getBestSellers(Pageable pageable) {
        Page<Product> productPage = productRepository.findBestSellers(pageable);
        return pagingHelper.toPaginatedResponse(productPage, productMapper::toProductResponseWithoutRelations);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'new-arrivals-' + #pageable.pageNumber")
    public PaginatedResponse<ProductResponse> getNewArrivals(Pageable pageable) {
        Page<Product> productPage = productRepository.findNewArrivals(pageable);
        return pagingHelper.toPaginatedResponse(productPage, productMapper::toProductResponseWithoutRelations);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ProductResponse> getTopRated(Pageable pageable) {
        Page<Product> productPage = productRepository.findTopRated(pageable);
        return pagingHelper.toPaginatedResponse(productPage, productMapper::toProductResponseWithoutRelations);
    }

    @Override
    @Transactional(readOnly = true)
    @Retry(name = "productSearch")
    @CircuitBreaker(name = "elasticsearchService", fallbackMethod = "searchProductsFallback")
    public PaginatedResponse<ProductResponse> searchProducts(String query, Pageable pageable) {
        Page<com.picknquicks.domain.product.ProductSearchDocument> searchPage =
                searchRepository.searchProducts(query, pageable);

        List<UUID> productIds = searchPage.getContent().stream()
                .map(doc -> UUID.fromString(doc.getId()))
                .toList();

        List<Product> products = productRepository.findAllById(productIds);

        return PaginatedResponse.<ProductResponse>builder()
                .content(productMapper.toResponseList(products))
                .page(searchPage.getNumber())
                .size(searchPage.getSize())
                .totalElements(searchPage.getTotalElements())
                .totalPages(searchPage.getTotalPages())
                .first(searchPage.isFirst())
                .last(searchPage.isLast())
                .hasNext(searchPage.hasNext())
                .hasPrevious(searchPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ProductResponse> filterProducts(
            UUID categoryId, UUID brandId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {

        Page<Product> productPage;

        if (categoryId != null && brandId != null) {
            productPage = productRepository.findAll(pageable);
        } else if (categoryId != null) {
            productPage = productRepository.findByCategoryId(categoryId, pageable);
        } else if (brandId != null) {
            productPage = productRepository.findByBrandId(brandId, pageable);
        } else if (minPrice != null && maxPrice != null) {
            productPage = productRepository.findByPriceRange(minPrice, maxPrice, pageable);
        } else {
            productPage = productRepository.findAllActive(pageable);
        }

        return pagingHelper.toPaginatedResponse(productPage, productMapper::toProductResponseWithoutRelations);
    }

    @Override
    @Transactional
    @CacheEvict(value = "product", key = "#productId")
    public ProductResponse addProductImage(UUID productId, MultipartFile imageFile, String altText, Boolean isPrimary) {
        Product product = findProductOrThrow(productId);

        String imageUrl = storeFile(imageFile, "product");

        if (Boolean.TRUE.equals(isPrimary)) {
            product.getImages().forEach(img -> img.setIsPrimary(false));
        }

        ProductImage productImage = ProductImage.builder()
                .imageUrl(imageUrl)
                .altText(altText)
                .isPrimary(isPrimary != null ? isPrimary : false)
                .displayOrder(product.getImages().size())
                .build();

        product.addImage(productImage);
        Product savedProduct = productRepository.save(product);
        searchRepository.save(productMapper.toSearchDocument(savedProduct));

        return productMapper.toProductResponse(savedProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = "product", key = "#productId")
    public void removeProductImage(UUID productId, UUID imageId) {
        Product product = findProductOrThrow(productId);

        ProductImage imageToRemove = product.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

        fileStorageService.deleteFile(imageToRemove.getImageUrl());
        product.removeImage(imageToRemove);

        Product savedProduct = productRepository.save(product);
        searchRepository.save(productMapper.toSearchDocument(savedProduct));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public ProductResponse updateStock(UUID productId, Integer quantity) {
        Product product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Integer oldStock = product.getStockQuantity();

        if (quantity < 0) {
            product.decrementStock(Math.abs(quantity));
        } else {
            product.incrementStock(quantity);
        }

        Product savedProduct = productRepository.save(product);
        searchRepository.save(productMapper.toSearchDocument(savedProduct));

        eventPublisher.publishEvent(new ProductStockChangedEvent(this, productId, oldStock, savedProduct.getStockQuantity()));

        log.info("Updated stock for product {}: {} -> {}", productId, oldStock, savedProduct.getStockQuantity());

        return productMapper.toProductResponse(savedProduct);
    }

    @Override
    @Transactional
    public void incrementViewCount(UUID productId) {
        productRepository.incrementViewCount(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts() {
        List<Product> products = productRepository.findLowStockProducts();
        return productMapper.toResponseList(products);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getOutOfStockProducts() {
        List<Product> products = productRepository.findOutOfStockProducts();
        return productMapper.toResponseList(products);
    }

    private Product findProductOrThrow(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
    }

    private void validateUniqueness(String slug, String sku, UUID excludeProductId) {
        validateSlugUniqueness(slug, excludeProductId);
        validateSkuUniqueness(sku, excludeProductId);
    }

    private void validateSlugUniqueness(String slug, UUID excludeProductId) {
        if (excludeProductId != null) {
            boolean exists = productRepository.findBySlug(slug)
                    .map(product -> !product.getId().equals(excludeProductId))
                    .orElse(false);
            if (exists) {
                throw new BadRequestException("Slug already exists: " + slug);
            }
        } else {
            if (productRepository.existsBySlug(slug)) {
                throw new BadRequestException("Slug already exists: " + slug);
            }
        }
    }

    private void validateSkuUniqueness(String sku, UUID excludeProductId) {
        if (excludeProductId != null) {
            boolean exists = productRepository.findBySku(sku)
                    .map(product -> !product.getId().equals(excludeProductId))
                    .orElse(false);
            if (exists) {
                throw new BadRequestException("SKU already exists: " + sku);
            }
        } else {
            if (productRepository.existsBySku(sku)) {
                throw new BadRequestException("SKU already exists: " + sku);
            }
        }
    }

    private <T> void updateIfPresent(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private String storeFile(MultipartFile file, String entityType) {
        try {
            return fileStorageService.storeFile(file, entityType);
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    private ProductResponse getProductBySlugFallback(String slug, Exception ex) {
        log.error("Circuit breaker fallback for getProductBySlug: {}", slug, ex);
        throw new ResourceNotFoundException("Product service temporarily unavailable");
    }

    private PaginatedResponse<ProductResponse> searchProductsFallback(String query, Pageable pageable, Exception ex) {
        log.error("Search service fallback for query: {}", query, ex);
        Page<Product> productPage = productRepository.search(query, pageable);
        return pagingHelper.toPaginatedResponse(productPage, productMapper::toProductResponseWithoutRelations);
    }
}