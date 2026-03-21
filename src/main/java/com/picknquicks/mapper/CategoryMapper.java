package com.picknquicks.mapper;

import com.picknquicks.domain.category.Category;
import com.picknquicks.dto.response.CategoryResponse;
import com.picknquicks.dto.response.CategoryTreeResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    public CategoryResponse toCategoryResponse(Category category, boolean includeChildren) {
        CategoryResponse.CategoryResponseBuilder builder = CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .iconUrl(category.getIconUrl())
                .active(category.getActive())
                .displayOrder(category.getDisplayOrder())
                .level(category.getLevel())
                .fullPath(category.getFullPath())
                .hasChildren(category.hasChildren())
                .childrenCount(category.getChildren() != null ? category.getChildren().size() : 0)
                .metaTitle(category.getMetaTitle())
                .metaDescription(category.getMetaDescription())
                .metaKeywords(category.getMetaKeywords())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt());

        if (category.getParent() != null) {
            builder.parentId(category.getParent().getId())
                    .parentName(category.getParent().getName());
        }

        if (includeChildren && category.hasChildren()) {
            Set<CategoryResponse> children = category.getChildren().stream()
                    .map(child -> toCategoryResponse(child, false))
                    .collect(Collectors.toSet());
            builder.children(children);
        }

        return builder.build();
    }

    public CategoryResponse toCategoryResponse(Category category) {
        return toCategoryResponse(category, false);
    }

    public CategoryTreeResponse toCategoryTreeResponse(Category category) {
        List<CategoryTreeResponse> children = category.getChildren() != null
                ? category.getChildren().stream()
                .map(this::toCategoryTreeResponse)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return CategoryTreeResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .iconUrl(category.getIconUrl())
                .active(category.getActive())
                .displayOrder(category.getDisplayOrder())
                .level(category.getLevel())
                .children(children)
                .build();
    }

    public CategoryTreeResponse toActiveCategoryTreeResponse(Category category) {
        List<CategoryTreeResponse> children = category.getChildren() != null
                ? category.getChildren().stream()
                .filter(Category::getActive)
                .map(this::toActiveCategoryTreeResponse)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return CategoryTreeResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .iconUrl(category.getIconUrl())
                .active(category.getActive())
                .displayOrder(category.getDisplayOrder())
                .level(category.getLevel())
                .children(children)
                .build();
    }


    public List<CategoryResponse> toResponseList(List<Category> categories, boolean includeChildren) {
        return categories.stream()
                .map(cat -> toCategoryResponse(cat, includeChildren))
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> toResponseList(List<Category> categories) {
        return toResponseList(categories, false);
    }

    public List<CategoryTreeResponse> toTreeResponseList(List<Category> categories) {
        return categories.stream()
                .map(this::toCategoryTreeResponse)
                .collect(Collectors.toList());
    }

    public List<CategoryTreeResponse> toActiveTreeResponseList(List<Category> categories) {
        return categories.stream()
                .map(this::toActiveCategoryTreeResponse)
                .collect(Collectors.toList());
    }
}

