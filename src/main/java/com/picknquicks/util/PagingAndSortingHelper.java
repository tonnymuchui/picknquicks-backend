package com.picknquicks.util;

import com.picknquicks.dto.response.PaginatedResponse;
import com.picknquicks.exception.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PagingAndSortingHelper {

    public Pageable buildPageable(int page, int size, String sortBy, String sortDirection) {
        if (page < 0) {
            throw new BadRequestException("Page must be greater than or equal to 0");
        }
        if (size < 1 || size > 100) {
            throw new BadRequestException("Size must be between 1 and 100");
        }

        String resolvedSortBy = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
        String resolvedSortDirection = (sortDirection == null || sortDirection.isBlank()) ? "DESC" : sortDirection;

        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(resolvedSortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC,
                resolvedSortBy
        );
        return PageRequest.of(page, size, sort);
    }

    public <E, T> PaginatedResponse<T> toPaginatedResponse(Page<E> page, Function<E, T> mapper) {
        return PaginatedResponse.<T>builder()
                .content(page.getContent().stream()
                        .map(mapper)
                        .collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}

