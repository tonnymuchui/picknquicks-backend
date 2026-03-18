package com.picknquicks.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Paginated response wrapper")
public class PaginatedResponse<T> {

    @Schema(description = "Page content")
    private List<T> content;

    @Schema(description = "Current page number")
    private Integer page;

    @Schema(description = "Page size")
    private Integer size;

    @Schema(description = "Total number of elements")
    private Long totalElements;

    @Schema(description = "Total number of pages")
    private Integer totalPages;

    @Schema(description = "Is first page")
    private Boolean first;

    @Schema(description = "Is last page")
    private Boolean last;

    @Schema(description = "Has next page")
    private Boolean hasNext;

    @Schema(description = "Has previous page")
    private Boolean hasPrevious;
}