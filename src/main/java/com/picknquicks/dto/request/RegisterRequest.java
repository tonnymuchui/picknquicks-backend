package com.picknquicks.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User registration request")
public class RegisterRequest {

    @Schema(description = "User email address", example = "john.doe@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 128, message = "Email must not exceed 128 characters")
    private String email;

    @Schema(description = "User password", example = "SecurePass123!")
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character"
    )
    private String password;

    @Schema(description = "Confirm password", example = "SecurePass123!")
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @Schema(description = "First name", example = "John")
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 64, message = "First name must be between 2 and 64 characters")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 64, message = "Last name must be between 2 and 64 characters")
    private String lastName;

    @Schema(description = "Phone number", example = "+254712345678")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;
}