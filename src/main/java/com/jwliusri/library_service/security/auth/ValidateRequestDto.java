package com.jwliusri.library_service.security.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidateRequestDto {
    private String requestId;
    private int otp;
}
