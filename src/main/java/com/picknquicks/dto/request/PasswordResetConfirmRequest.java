package com.picknquicks.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Password reset confirmation request")
public class PasswordResetConfirmRequest {

    @Schema(description = "Reset token", example = "abc123def456")
    @NotBlank(message = "Token is required")
    private String token;

    @Schema(description = "New password", example = "NewSecurePass123!")
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character"
    )
    private String newPassword;

    @Schema(description = "Confirm new password", example = "NewSecurePass123!")
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}