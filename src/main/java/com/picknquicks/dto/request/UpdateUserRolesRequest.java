package com.picknquicks.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update user roles request")
public class UpdateUserRolesRequest {

    @Schema(description = "List of role names to assign to user", example = "[\"ADMIN\", \"CUSTOMER\"]")
    @NotEmpty(message = "At least one role is required")
    private Set<String> roles;
}