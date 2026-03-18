package com.picknquicks.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @Size(min = 2, max = 64, message = "First name must be between 2 and 64 characters")
    private String firstName;

    @Size(min = 2, max = 64, message = "Last name must be between 2 and 64 characters")
    private String lastName;

    @Size(min = 10, max = 20, message = "Phone must be between 10 and 20 characters")
    private String phone;

    private MultipartFile avatarFile;
}
