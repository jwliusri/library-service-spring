package com.jwliusri.library_service.security;
import com.jwliusri.library_service.user.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jwliusri.library_service.user.User;

@Service
public class LoginAttemptService {

    private final UserRepository userRepository;
    @Value("${security.max-failed-attempts}")
    private int maxAttempts;

    @Value("${security.attempt-window-minutes}")
    private int attemptWindowMinutes;
    
    @Value("${security.block-time-minutes}")
    private int blockTimeMinutes;

    LoginAttemptService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void loginFailed(String usernameOrEmail) {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
            .orElseThrow();
        
        if (user.isAccountNonLocked()) {
            if (user.getFailedAttemptTime() == null) {
                user.setFailedAttemptTime(LocalDateTime.now());
            } else if (user.getFailedAttemptTime().atZone(ZoneOffset.systemDefault()).toEpochSecond() * 1000 < System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(attemptWindowMinutes)) {
                user.setFailedAttempt(0);
                user.setFailedAttemptTime(LocalDateTime.now());
            }

            if (user.getFailedAttempt() < maxAttempts - 1) {
                user.setFailedAttempt(user.getFailedAttempt() + 1);
                userRepository.save(user);
            } else {
                user.setAccountNonLocked(false);
                user.setLockTime(LocalDateTime.now());
                userRepository.save(user);
            }
        }
    }

    public void resetAttempts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow();
        user.setFailedAttempt(0);
        user.setAccountNonLocked(true);
        user.setLockTime(null);
        user.setFailedAttemptTime(null);
        userRepository.save(user);
    }

    public boolean isLocked(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow();
            
        if (user.isAccountNonLocked()) {
            return false;
        }

        if (user.getLockTime().atZone(ZoneOffset.systemDefault()).toEpochSecond() * 1000 < System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(blockTimeMinutes) ) {
            return false;
        }

        return true;
    }

}
