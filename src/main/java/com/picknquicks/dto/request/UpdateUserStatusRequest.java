package com.picknquicks.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update user status request")
public class UpdateUserStatusRequest {

    @Schema(description = "Enable or disable user", example = "true")
    @NotNull(message = "Enabled status is required")
    private Boolean enabled;
}