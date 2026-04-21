package com.picknquicks.repository.product;
import com.picknquicks.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySku(String sku);

    boolean existsBySlug(String slug);

    boolean existsBySku(String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") UUID id);

    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.displayOrder ASC, p.createdAt DESC")
    Page<Product> findAllActive(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.featured = true AND p.active = true ORDER BY p.displayOrder ASC")
    List<Product> findFeaturedProducts();

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.active = true")
    Page<Product> findByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.brand.id = :brandId AND p.active = true")
    Page<Product> findByBrandId(@Param("brandId") UUID brandId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.salePrice IS NOT NULL AND p.salePrice < p.price AND p.active = true ORDER BY ((p.price - p.salePrice) / p.price) DESC")
    Page<Product> findOnSaleProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.saleCount DESC")
    Page<Product> findBestSellers(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.createdAt DESC")
    Page<Product> findNewArrivals(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.averageRating DESC, p.reviewCount DESC")
    Page<Product> findTopRated(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.stockQuantity <= p.lowStockThreshold AND p.active = true")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= 0 AND p.active = true")
    List<Product> findOutOfStockProducts();

    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Product> search(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.active = true")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

    @Modifying
    @Query("UPDATE Product p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Product p SET p.saleCount = p.saleCount + :quantity WHERE p.id = :id")
    void incrementSaleCount(@Param("id") UUID id, @Param("quantity") Long quantity);

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity WHERE p.id = :id AND p.stockQuantity >= :quantity")
    int decrementStock(@Param("id") UUID id, @Param("quantity") Integer quantity);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true")
    Long countActive();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.featured = true AND p.active = true")
    Long countFeatured();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.active = true")
    Long countByCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.brand.id = :brandId AND p.active = true")
    Long countByBrandId(@Param("brandId") UUID brandId);
}