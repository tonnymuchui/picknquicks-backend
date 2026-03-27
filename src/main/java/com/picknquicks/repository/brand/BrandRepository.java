package com.picknquicks.repository.brand;
import com.picknquicks.domain.brand.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID> {

    Optional<Brand> findBySlug(String slug);

    Optional<Brand> findByName(String name);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    @Query("SELECT b FROM Brand b WHERE b.active = true ORDER BY b.displayOrder ASC, b.name ASC")
    List<Brand> findAllActive();

    @Query("SELECT b FROM Brand b WHERE b.active = true")
    Page<Brand> findAllActive(Pageable pageable);

    @Query("SELECT b FROM Brand b WHERE b.featured = true AND b.active = true ORDER BY b.displayOrder ASC")
    List<Brand> findAllFeatured();

    @Query("SELECT b FROM Brand b WHERE " +
            "LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.countryOfOrigin) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Brand> search(@Param("search") String search, Pageable pageable);

    @Query("SELECT b FROM Brand b WHERE b.countryOfOrigin = :country AND b.active = true ORDER BY b.name ASC")
    List<Brand> findByCountry(@Param("country") String country);

    @Query("SELECT COUNT(b) FROM Brand b WHERE b.active = true")
    Long countActive();

    @Query("SELECT COUNT(b) FROM Brand b WHERE b.featured = true AND b.active = true")
    Long countFeatured();

    @Query("SELECT DISTINCT b.countryOfOrigin FROM Brand b WHERE b.countryOfOrigin IS NOT NULL AND b.active = true ORDER BY b.countryOfOrigin ASC")
    List<String> findAllCountries();
}