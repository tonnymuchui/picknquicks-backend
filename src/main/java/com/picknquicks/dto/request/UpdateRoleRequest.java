package com.picknquicks.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update role request")
public class UpdateRoleRequest {

    @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
    @Schema(description = "Role name", example = "MODERATOR")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Schema(description = "Role description", example = "Updated description")
    private String description;
}

