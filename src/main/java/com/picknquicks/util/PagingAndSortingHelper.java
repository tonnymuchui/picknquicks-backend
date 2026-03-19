package com.picknquicks.util;

import com.picknquicks.dto.response.PaginatedResponse;
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
        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy
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

