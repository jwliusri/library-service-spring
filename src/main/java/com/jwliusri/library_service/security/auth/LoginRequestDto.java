package com.jwliusri.library_service.security.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDto {
    @NotBlank(message = "usernameOrEmail is required")
    private String usernameOrEmail;

    @NotBlank(message = "password is required")
    private String password;
}
