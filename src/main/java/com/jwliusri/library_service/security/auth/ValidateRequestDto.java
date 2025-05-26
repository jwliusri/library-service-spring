package com.jwliusri.library_service.security.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidateRequestDto {
    private String requestId;
    private int otp;
}
