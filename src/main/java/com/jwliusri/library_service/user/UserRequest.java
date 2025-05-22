package com.jwliusri.library_service.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRequest {
    @NotBlank(message = "fullName is required")
    private String fullName;
    @NotBlank(message = "username is required")
    private String username;
    @NotBlank(message = "email is required")
    private String email;

    private String password;

    @Builder.Default
    private RoleEnum role = RoleEnum.ROLE_VIEWER;
}
