package com.jwliusri.library_service.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {
    @NotBlank(message = "fullName is required")
    private String fullName;
    @NotBlank(message = "username is required")
    private String username;
    @NotBlank(message = "email is required")
    private String email;

    private String password;
    private RoleEnum role;
}
