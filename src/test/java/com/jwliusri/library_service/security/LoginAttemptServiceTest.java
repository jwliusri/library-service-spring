package com.jwliusri.library_service.security;

import com.jwliusri.library_service.user.User;
import com.jwliusri.library_service.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoginAttemptService loginAttemptService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Initialize test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setAccountNonLocked(true);
        testUser.setFailedAttempt(0);
        testUser.setLockTime(null);
        testUser.setFailedAttemptTime(null);

        // Set test properties
        ReflectionTestUtils.setField(loginAttemptService, "maxAttempts", 5);
        ReflectionTestUtils.setField(loginAttemptService, "attemptWindowMinutes", 10);
        ReflectionTestUtils.setField(loginAttemptService, "blockTimeMinutes", 30);
    }

    @Test
    void loginFailed_ShouldIncrementFailedAttempts() {
        // Arrange
        when(userRepository.findByUsernameOrEmail(any(), any())).thenReturn(java.util.Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        loginAttemptService.loginFailed("testuser");

        // Assert
        assertEquals(1, testUser.getFailedAttempt());
        assertNotNull(testUser.getFailedAttemptTime());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void loginFailed_WhenMaxAttemptsReached_ShouldLockAccount() {
        // Arrange
        testUser.setFailedAttempt(4); // One below max
        when(userRepository.findByUsernameOrEmail(any(), any())).thenReturn(java.util.Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        loginAttemptService.loginFailed("testuser");

        // Assert
        assertFalse(testUser.isAccountNonLocked());
        assertNotNull(testUser.getLockTime());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void loginFailed_WhenWindowExpired_ShouldResetAttempts() {
        // Arrange
        testUser.setFailedAttempt(2);
        testUser.setFailedAttemptTime(LocalDateTime.now().minusMinutes(15)); // Outside window
        when(userRepository.findByUsernameOrEmail(any(), any())).thenReturn(java.util.Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        loginAttemptService.loginFailed("testuser");

        // Assert
        assertEquals(1, testUser.getFailedAttempt()); // Reset to 0 then incremented to 1
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void loginFailed_WhenAccountAlreadyLocked_ShouldNotChange() {
        // Arrange
        testUser.setAccountNonLocked(false);
        testUser.setLockTime(LocalDateTime.now());
        when(userRepository.findByUsernameOrEmail(any(), any())).thenReturn(java.util.Optional.of(testUser));

        // Act
        loginAttemptService.loginFailed("testuser");

        // Assert
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetAttempts_ShouldResetAllSecurityFields() {
        // Arrange
        testUser.setFailedAttempt(3);
        testUser.setAccountNonLocked(false);
        testUser.setLockTime(LocalDateTime.now());
        testUser.setFailedAttemptTime(LocalDateTime.now());
        when(userRepository.findByUsername(any())).thenReturn(java.util.Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        loginAttemptService.resetAttempts("testuser");

        // Assert
        assertEquals(0, testUser.getFailedAttempt());
        assertTrue(testUser.isAccountNonLocked());
        assertNull(testUser.getLockTime());
        assertNull(testUser.getFailedAttemptTime());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void isLocked_WhenAccountNotLocked_ShouldReturnFalse() {
        // Arrange
        when(userRepository.findByUsername(any())).thenReturn(java.util.Optional.of(testUser));

        // Act
        boolean result = loginAttemptService.isLocked("testuser");

        // Assert
        assertFalse(result);
    }

    @Test
    void isLocked_WhenAccountLockedWithinBlockTime_ShouldReturnTrue() {
        // Arrange
        testUser.setAccountNonLocked(false);
        testUser.setLockTime(LocalDateTime.now()); // Just now
        when(userRepository.findByUsername(any())).thenReturn(java.util.Optional.of(testUser));

        // Act
        boolean result = loginAttemptService.isLocked("testuser");

        // Assert
        assertTrue(result);
    }

    @Test
    void isLocked_WhenAccountLockedPastBlockTime_ShouldReturnFalse() {
        // Arrange
        testUser.setAccountNonLocked(false);
        testUser.setLockTime(LocalDateTime.now().minusMinutes(35)); // Past block time
        when(userRepository.findByUsername(any())).thenReturn(java.util.Optional.of(testUser));

        // Act
        boolean result = loginAttemptService.isLocked("testuser");

        // Assert
        assertFalse(result);
    }

    @Test
    void isLocked_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername(any())).thenReturn(java.util.Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> loginAttemptService.isLocked("unknown"));
    }
}