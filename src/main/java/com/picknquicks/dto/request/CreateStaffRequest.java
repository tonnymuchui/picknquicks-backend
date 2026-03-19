package com.picknquicks.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create staff member request")
public class CreateStaffRequest {

    @Schema(description = "Email address", example = "staff@picknquicks.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 128, message = "Email must not exceed 128 characters")
    private String email;

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

    @Schema(description = "Roles to assign", example = "[\"STAFF\", \"MANAGER\"]")
    @NotEmpty(message = "At least one role is required")
    private Set<String> roles;
}