package com.jwliusri.library_service.user;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private RoleEnum role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
