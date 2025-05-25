package com.jwliusri.library_service.security.mfa;

import org.springframework.data.redis.core.RedisHash;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("mfa_sessions")
public class MfaOtp {

    @Id
    private String id;
    private String username;
    private int otp;
}
