package com.picknquicks.repository.product;

import com.picknquicks.domain.product.ProductSearchDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductSearchDocument, String> {

    Page<ProductSearchDocument> findByActiveTrue(Pageable pageable);

    Page<ProductSearchDocument> findByFeaturedTrueAndActiveTrue(Pageable pageable);

    Page<ProductSearchDocument> findByCategoryIdAndActiveTrue(String categoryId, Pageable pageable);

    Page<ProductSearchDocument> findByBrandIdAndActiveTrue(String brandId, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name^3\", \"description\", \"brandName\", \"categoryName\"], \"fuzziness\": \"AUTO\"}}], \"filter\": [{\"term\": {\"active\": true}}]}}")
    Page<ProductSearchDocument> searchProducts(String query, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"range\": {\"price\": {\"gte\": ?0, \"lte\": ?1}}}], \"filter\": [{\"term\": {\"active\": true}}]}}")
    Page<ProductSearchDocument> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"exists\": {\"field\": \"salePrice\"}}], \"filter\": [{\"term\": {\"active\": true}}]}}")
    Page<ProductSearchDocument> findOnSaleProducts(Pageable pageable);

    List<ProductSearchDocument> findTop10ByActiveTrueOrderBySaleCountDesc();

    List<ProductSearchDocument> findTop10ByActiveTrueOrderByCreatedAtDesc();
}