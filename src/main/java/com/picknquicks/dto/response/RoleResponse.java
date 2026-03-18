package com.picknquicks.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Role response")
public class RoleResponse {

    @Schema(description = "Role ID")
    private UUID id;

    @Schema(description = "Role name")
    private String name;

    @Schema(description = "Role description")
    private String description;

    @Schema(description = "Number of users with this role")
    private long userCount;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;

    @Schema(description = "Updated at")
    private LocalDateTime updatedAt;
}

