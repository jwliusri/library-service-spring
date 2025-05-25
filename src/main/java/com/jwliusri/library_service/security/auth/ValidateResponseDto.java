package com.jwliusri.library_service.security.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidateResponseDto {
    private Long id;
    private String token;
}
