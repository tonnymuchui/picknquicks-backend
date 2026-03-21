package com.picknquicks.service.category;

import com.picknquicks.dto.request.CreateCategoryRequest;
import com.picknquicks.dto.request.UpdateCategoryRequest;
import com.picknquicks.dto.response.CategoryResponse;
import com.picknquicks.dto.response.CategoryTreeResponse;
import com.picknquicks.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryResponse createCategory(CreateCategoryRequest request);
    CategoryResponse updateCategory(UUID categoryId, UpdateCategoryRequest request);
    void deleteCategory(UUID categoryId);
    CategoryResponse getCategoryById(UUID categoryId);
    CategoryResponse getCategoryBySlug(String slug);
    PaginatedResponse<CategoryResponse> getAllCategories(Pageable pageable);
    List<CategoryResponse> getRootCategories();
    List<CategoryResponse> getActiveRootCategories();
    List<CategoryResponse> getChildCategories(UUID parentId);
    List<CategoryResponse> getActiveChildCategories(UUID parentId);
    List<CategoryTreeResponse> getCategoryTree();
    List<CategoryTreeResponse> getActiveCategoryTree();
    PaginatedResponse<CategoryResponse> searchCategories(String search, Pageable pageable);
    CategoryResponse moveCategory(UUID categoryId, UUID newParentId);
    void reorderCategories(List<UUID> categoryIds);

    CategoryResponse uploadCategoryImage(UUID categoryId, MultipartFile imageFile);
    CategoryResponse uploadCategoryIcon(UUID categoryId, MultipartFile iconFile);
    CategoryResponse removeCategoryImage(UUID categoryId);
    CategoryResponse removeCategoryIcon(UUID categoryId);
}