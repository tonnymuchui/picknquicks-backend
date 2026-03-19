package com.picknquicks.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User search filters")
public class UserSearchRequest {

    @Schema(description = "Search by email, first name, or last name")
    private String search;

    @Schema(description = "Filter by role", example = "ADMIN")
    private String role;

    @Schema(description = "Filter by email verified status")
    private Boolean emailVerified;

    @Schema(description = "Filter by enabled status")
    private Boolean enabled;

    @Schema(description = "Page number", example = "0")
    private Integer page = 0;

    @Schema(description = "Page size", example = "20")
    private Integer size = 20;

    @Schema(description = "Sort field", example = "createdAt")
    private String sortBy = "createdAt";

    @Schema(description = "Sort direction", example = "DESC")
    private String sortDirection = "DESC";
}