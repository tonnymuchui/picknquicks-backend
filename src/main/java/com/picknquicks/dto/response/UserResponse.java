package com.picknquicks.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.picknquicks.security.AuthenticationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User information response")
public class UserResponse {

    @Schema(description = "User ID")
    private UUID id;

    @Schema(description = "Email address")
    private String email;

    @Schema(description = "First name")
    private String firstName;

    @Schema(description = "Last name")
    private String lastName;

    @Schema(description = "Full name")
    private String fullName;

    @Schema(description = "Phone number")
    private String phone;

    @Schema(description = "Avatar URL")
    private String avatarUrl;

    @Schema(description = "Account enabled status")
    private Boolean enabled;

    @Schema(description = "Email verified status")
    private Boolean emailVerified;

    @Schema(description = "Authentication provider")
    private AuthenticationType provider;

    @Schema(description = "User roles")
    private Set<String> roles;

    @Schema(description = "Account creation time")
    private LocalDateTime createdAt;

    @Schema(description = "Last login time")
    private LocalDateTime lastLogin;
}