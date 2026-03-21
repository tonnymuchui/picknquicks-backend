package com.picknquicks.controller;
import com.picknquicks.dto.request.CreateCategoryRequest;
import com.picknquicks.dto.request.UpdateCategoryRequest;
import com.picknquicks.dto.response.ApiResponse;
import com.picknquicks.dto.response.CategoryResponse;
import com.picknquicks.dto.response.CategoryTreeResponse;
import com.picknquicks.dto.response.PaginatedResponse;
import com.picknquicks.service.category.CategoryService;
import com.picknquicks.util.PagingAndSortingHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

    private final CategoryService categoryService;
    private final PagingAndSortingHelper pagingHelper;

    @GetMapping
    @Operation(summary = "Get all categories", description = "Get paginated list of all categories")
    public ResponseEntity<ApiResponse> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        Pageable pageable = pagingHelper.buildPageable(page, size, sortBy, sortDirection);
        PaginatedResponse<CategoryResponse> response = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(ApiResponse.success("Categories fetched successfully", response));
    }

    @GetMapping("/roots")
    @Operation(summary = "Get root categories", description = "Get all root categories (no parent)")
    public ResponseEntity<ApiResponse> getRootCategories() {
        return ResponseEntity.ok(ApiResponse.success("Root categories fetched successfully", categoryService.getRootCategories()));
    }

    @GetMapping("/roots/active")
    @Operation(summary = "Get active root categories")
    public ResponseEntity<ApiResponse> getActiveRootCategories() {
        return ResponseEntity.ok(ApiResponse.success("Active root categories fetched successfully", categoryService.getActiveRootCategories()));
    }

    @GetMapping("/tree")
    @Operation(summary = "Get category tree", description = "Get hierarchical category tree")
    public ResponseEntity<ApiResponse> getCategoryTree() {
        List<CategoryTreeResponse> response = categoryService.getCategoryTree();
        return ResponseEntity.ok(ApiResponse.success("Category tree fetched successfully", response));
    }

    @GetMapping("/tree/active")
    @Operation(summary = "Get active category tree")
    public ResponseEntity<ApiResponse> getActiveCategoryTree() {
        List<CategoryTreeResponse> response = categoryService.getActiveCategoryTree();
        return ResponseEntity.ok(ApiResponse.success("Active category tree fetched successfully", response));
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse> getCategoryById(@PathVariable UUID categoryId) {
        CategoryResponse response = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category fetched successfully", response));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug")
    public ResponseEntity<ApiResponse> getCategoryBySlug(@PathVariable String slug) {
        CategoryResponse response = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Category fetched successfully", response));
    }

    @GetMapping("/{parentId}/children")
    @Operation(summary = "Get child categories")
    public ResponseEntity<ApiResponse> getChildCategories(@PathVariable UUID parentId) {
        return ResponseEntity.ok(ApiResponse.success("Child categories fetched successfully", categoryService.getChildCategories(parentId)));
    }

    @GetMapping("/{parentId}/children/active")
    @Operation(summary = "Get active child categories")
    public ResponseEntity<ApiResponse> getActiveChildCategories(@PathVariable UUID parentId) {
        return ResponseEntity.ok(ApiResponse.success("Active child categories fetched successfully", categoryService.getActiveChildCategories(parentId)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search categories")
    public ResponseEntity<ApiResponse> searchCategories(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        Pageable pageable = pagingHelper.buildPageable(page, size, sortBy, sortDirection);
        PaginatedResponse<CategoryResponse> response = categoryService.searchCategories(query, pageable);
        return ResponseEntity.ok(ApiResponse.success("Categories search completed successfully", response));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create category")
    public ResponseEntity<ApiResponse> createCategory(@Valid @ModelAttribute CreateCategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.ok(ApiResponse.success("Category created successfully", response));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create category")
    public ResponseEntity<ApiResponse> createCategoryJson(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.ok(ApiResponse.success("Category created successfully", response));
    }

    @PutMapping(value = "/{categoryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update category")
    public ResponseEntity<ApiResponse> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @ModelAttribute UpdateCategoryRequest request
    ) {
        CategoryResponse response = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", response));
    }

    @PutMapping(value = "/{categoryId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update category")
    public ResponseEntity<ApiResponse> updateCategoryJson(
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        CategoryResponse response = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", response));
    }

    @PostMapping(value = "/{categoryId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Upload category image")
    public ResponseEntity<ApiResponse> uploadCategoryImage(
            @PathVariable UUID categoryId,
            @RequestParam("file") MultipartFile file
    ) {
        CategoryResponse response = categoryService.uploadCategoryImage(categoryId, file);
        return ResponseEntity.ok(ApiResponse.success("Category image uploaded successfully", response));
    }

    @PostMapping(value = "/{categoryId}/icon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Upload category icon")
    public ResponseEntity<ApiResponse> uploadCategoryIcon(
            @PathVariable UUID categoryId,
            @RequestParam("file") MultipartFile file
    ) {
        CategoryResponse response = categoryService.uploadCategoryIcon(categoryId, file);
        return ResponseEntity.ok(ApiResponse.success("Category icon uploaded successfully", response));
    }

    @DeleteMapping("/{categoryId}/image")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Remove category image")
    public ResponseEntity<ApiResponse> removeCategoryImage(@PathVariable UUID categoryId) {
        CategoryResponse response = categoryService.removeCategoryImage(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category image removed successfully", response));
    }

    @DeleteMapping("/{categoryId}/icon")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Remove category icon")
    public ResponseEntity<ApiResponse> removeCategoryIcon(@PathVariable UUID categoryId) {
        CategoryResponse response = categoryService.removeCategoryIcon(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category icon removed successfully", response));
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete category")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable UUID categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully"));
    }

    @PatchMapping("/{categoryId}/move")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Move category to new parent")
    public ResponseEntity<ApiResponse> moveCategory(
            @PathVariable UUID categoryId,
            @RequestParam(required = false) UUID newParentId
    ) {
        CategoryResponse response = categoryService.moveCategory(categoryId, newParentId);
        return ResponseEntity.ok(ApiResponse.success("Category moved successfully", response));
    }

    @PatchMapping("/reorder")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Reorder categories")
    public ResponseEntity<ApiResponse> reorderCategories(@RequestBody List<UUID> categoryIds) {
        categoryService.reorderCategories(categoryIds);
        return ResponseEntity.ok(ApiResponse.success("Categories reordered successfully"));
    }
}