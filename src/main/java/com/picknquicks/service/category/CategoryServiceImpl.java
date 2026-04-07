package com.picknquicks.service.category;

import com.picknquicks.domain.category.Category;
import com.picknquicks.dto.request.CreateCategoryRequest;
import com.picknquicks.dto.request.UpdateCategoryRequest;
import com.picknquicks.dto.response.CategoryResponse;
import com.picknquicks.dto.response.CategoryTreeResponse;
import com.picknquicks.dto.response.PaginatedResponse;
import com.picknquicks.exception.BadRequestException;
import com.picknquicks.exception.ResourceNotFoundException;
import com.picknquicks.mapper.CategoryMapper;
import com.picknquicks.repository.category.CategoryRepository;
import com.picknquicks.service.storage.FileStorageService;
import com.picknquicks.util.PagingAndSortingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final PagingAndSortingHelper pagingHelper;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        validateSlugUniqueness(request.getSlug(), null);

        Category category = Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .imageUrl(resolveCreateFilePath(request.getImageFile(), request.getImageUrl(), "category"))
                .iconUrl(resolveCreateFilePath(request.getIconFile(), request.getIconUrl(), "category"))
                .active(request.getActive() != null ? request.getActive() : true)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .metaKeywords(request.getMetaKeywords())
                .build();

        if (request.getParentId() != null) {
            Category parent = findCategoryOrThrow(request.getParentId(), "Parent category not found");
            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("Created category: {} with ID: {}", savedCategory.getName(), savedCategory.getId());

        return categoryMapper.toCategoryResponse(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID categoryId, UpdateCategoryRequest request) {
        Category category = findCategoryOrThrow(categoryId, "Category not found");

        if (request.getSlug() != null && !request.getSlug().equals(category.getSlug())) {
            validateSlugUniqueness(request.getSlug(), categoryId);
            category.setSlug(request.getSlug());
        }

        updateIfPresent(request.getName(), category::setName);
        updateIfPresent(request.getDescription(), category::setDescription);
        updateIfPresent(request.getActive(), category::setActive);
        updateIfPresent(request.getDisplayOrder(), category::setDisplayOrder);
        updateIfPresent(request.getMetaTitle(), category::setMetaTitle);
        updateIfPresent(request.getMetaDescription(), category::setMetaDescription);
        updateIfPresent(request.getMetaKeywords(), category::setMetaKeywords);

        if (request.getParentId() != null) {
            validateParentChange(categoryId, request.getParentId(), category);
            Category newParent = findCategoryOrThrow(request.getParentId(), "Parent category not found");
            category.setParent(newParent);
        }

        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            category.setImageUrl(replaceFile(category.getImageUrl(), request.getImageFile(), "category"));
        } else if (request.getImageUrl() != null) {
            category.setImageUrl(request.getImageUrl());
        }

        if (request.getIconFile() != null && !request.getIconFile().isEmpty()) {
            category.setIconUrl(replaceFile(category.getIconUrl(), request.getIconFile(), "category"));
        } else if (request.getIconUrl() != null) {
            category.setIconUrl(request.getIconUrl());
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("Updated category with ID: {}", categoryId);

        return categoryMapper.toCategoryResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID categoryId) {
        Category category = findCategoryOrThrow(categoryId, "Category not found");

        if (category.hasChildren()) {
            throw new BadRequestException("Cannot delete category with children. Delete or move children first.");
        }

        deleteFileIfPresent(category.getImageUrl());
        deleteFileIfPresent(category.getIconUrl());

        categoryRepository.delete(category);
        log.info("Deleted category with ID: {}", categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID categoryId) {
        Category category = findCategoryOrThrow(categoryId, "Category not found");
        return categoryMapper.toCategoryResponse(category, true);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
        return categoryMapper.toCategoryResponse(category, true);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<CategoryResponse> getAllCategories(Pageable pageable) {
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        return pagingHelper.toPaginatedResponse(categoryPage, categoryMapper::toCategoryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        List<Category> categories = categoryRepository.findAllRootCategories();
        return categoryMapper.toResponseList(categories);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveRootCategories() {
        List<Category> categories = categoryRepository.findAllActiveRootCategories();
        return categoryMapper.toResponseList(categories);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getChildCategories(UUID parentId) {
        List<Category> categories = categoryRepository.findByParentId(parentId);
        return categoryMapper.toResponseList(categories);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveChildCategories(UUID parentId) {
        List<Category> categories = categoryRepository.findActiveByParentId(parentId);
        return categoryMapper.toResponseList(categories);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getCategoryTree() {
        List<Category> rootCategories = categoryRepository.findCategoryTree();
        return categoryMapper.toTreeResponseList(rootCategories);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getActiveCategoryTree() {
        List<Category> rootCategories = categoryRepository.findAllActiveRootCategories();
        return categoryMapper.toActiveTreeResponseList(rootCategories);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<CategoryResponse> searchCategories(String search, Pageable pageable) {
        Page<Category> categoryPage = categoryRepository.search(search, pageable);
        return pagingHelper.toPaginatedResponse(categoryPage, categoryMapper::toCategoryResponse);
    }

    @Override
    @Transactional
    public CategoryResponse moveCategory(UUID categoryId, UUID newParentId) {
        Category category = findCategoryOrThrow(categoryId, "Category not found");

        if (newParentId == null) {
            category.setParent(null);
        } else {
            validateParentChange(categoryId, newParentId, category);
            Category newParent = findCategoryOrThrow(newParentId, "Parent category not found");
            category.setParent(newParent);
        }

        Category movedCategory = categoryRepository.save(category);
        log.info("Moved category with ID: {} to parent: {}", categoryId, newParentId);

        return categoryMapper.toCategoryResponse(movedCategory);
    }

    @Override
    @Transactional
    public void reorderCategories(List<UUID> categoryIds) {
        for (int i = 0; i < categoryIds.size(); i++) {
            UUID categoryId = categoryIds.get(i);
            Category category = findCategoryOrThrow(categoryId, "Category not found");
            category.setDisplayOrder(i);
            categoryRepository.save(category);
        }
        log.info("Reordered {} categories", categoryIds.size());
    }

    @Override
    @Transactional
    public CategoryResponse uploadCategoryImage(UUID categoryId, MultipartFile imageFile) {
        Category category = findCategoryOrThrow(categoryId, "Category not found");
        category.setImageUrl(replaceFile(category.getImageUrl(), imageFile, "category"));
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(updatedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse uploadCategoryIcon(UUID categoryId, MultipartFile iconFile) {
        Category category = findCategoryOrThrow(categoryId, "Category not found");
        category.setIconUrl(replaceFile(category.getIconUrl(), iconFile, "category"));
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(updatedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse removeCategoryImage(UUID categoryId) {
        Category category = findCategoryOrThrow(categoryId, "Category not found");
        deleteFileIfPresent(category.getImageUrl());
        category.setImageUrl(null);
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(updatedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse removeCategoryIcon(UUID categoryId) {
        Category category = findCategoryOrThrow(categoryId, "Category not found");
        deleteFileIfPresent(category.getIconUrl());
        category.setIconUrl(null);
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(updatedCategory);
    }

    private Category findCategoryOrThrow(UUID categoryId, String errorMessage) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(errorMessage));
    }

    private void validateSlugUniqueness(String slug, UUID excludeCategoryId) {
        if (excludeCategoryId != null) {
            boolean exists = categoryRepository.findBySlug(slug)
                    .map(cat -> !cat.getId().equals(excludeCategoryId))
                    .orElse(false);
            if (exists) {
                throw new BadRequestException("Slug already exists: " + slug);
            }
        } else {
            if (categoryRepository.existsBySlug(slug)) {
                throw new BadRequestException("Slug already exists: " + slug);
            }
        }
    }

    private void validateParentChange(UUID categoryId, UUID newParentId, Category category) {
        if (newParentId.equals(categoryId)) {
            throw new BadRequestException("Category cannot be its own parent");
        }

        Category newParent = findCategoryOrThrow(newParentId, "Parent category not found");
        if (newParent.isDescendantOf(category)) {
            throw new BadRequestException("Cannot set descendant as parent (circular reference)");
        }
    }

    private <T> void updateIfPresent(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private String resolveCreateFilePath(MultipartFile file, String fallbackUrl, String entityType) {
        if (file == null || file.isEmpty()) {
            return fallbackUrl;
        }
        return storeFile(file, entityType);
    }

    private String replaceFile(String existingPath, MultipartFile file, String entityType) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        String newPath = storeFile(file, entityType);
        deleteFileIfPresent(existingPath);
        return newPath;
    }

    private String storeFile(MultipartFile file, String entityType) {
        try {
            return fileStorageService.storeFile(file, entityType);
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    private void deleteFileIfPresent(String filePath) {
        if (filePath != null && !filePath.isBlank()) {
            fileStorageService.deleteFile(filePath);
        }
    }
}