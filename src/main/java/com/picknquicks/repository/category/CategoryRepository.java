package com.picknquicks.repository.category;

import com.picknquicks.domain.category.Category;
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
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.displayOrder ASC")
    List<Category> findAllRootCategories();

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL")
    Page<Category> findAllRootCategories(Pageable pageable);

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.active = true ORDER BY c.displayOrder ASC")
    List<Category> findAllActiveRootCategories();

    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId ORDER BY c.displayOrder ASC")
    List<Category> findByParentId(@Param("parentId") UUID parentId);

    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId")
    Page<Category> findByParentId(@Param("parentId") UUID parentId, Pageable pageable);

    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.active = true ORDER BY c.displayOrder ASC")
    List<Category> findActiveByParentId(@Param("parentId") UUID parentId);

    @Query("SELECT c FROM Category c WHERE c.active = true ORDER BY c.displayOrder ASC")
    List<Category> findAllActive();

    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY c.displayOrder ASC")
    Page<Category> search(@Param("search") String search, Pageable pageable);

    @Query(value = """
        WITH RECURSIVE category_tree AS (
            SELECT id, name, slug, parent_id, 0 as level
            FROM categories
            WHERE parent_id IS NULL
            UNION ALL
            SELECT c.id, c.name, c.slug, c.parent_id, ct.level + 1
            FROM categories c
            INNER JOIN category_tree ct ON c.parent_id = ct.id
        )
        SELECT * FROM category_tree WHERE level = :level
        """, nativeQuery = true)
    List<Category> findByLevel(@Param("level") int level);

    @Query("SELECT COUNT(c) FROM Category c WHERE c.parent.id = :parentId")
    Long countByParentId(@Param("parentId") UUID parentId);

    @Query("SELECT COUNT(c) FROM Category c WHERE c.active = true")
    Long countActive();

    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.children WHERE c.parent IS NULL ORDER BY c.displayOrder ASC")
    List<Category> findCategoryTree();
}