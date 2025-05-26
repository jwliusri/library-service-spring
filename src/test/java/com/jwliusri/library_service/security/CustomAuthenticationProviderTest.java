package com.jwliusri.library_service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationProviderTest {

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private CustomAuthenticationProvider authenticationProvider;

    private UserDetails validUser;
    private Authentication authentication;
    private final String username = "testuser";
    private final String password = "password";
    private final String encodedPassword = "encodedPassword";

    @BeforeEach
    void setUp() {
        validUser = User.builder()
                .username(username)
                .password(encodedPassword)
                .authorities(Collections.emptyList())
                .build();

        authentication = new UsernamePasswordAuthenticationToken(username, password);
    }

    @Test
    void authenticate_WithValidCredentials_ShouldReturnAuthentication() {
        // Arrange
        when(userDetailsService.loadUserByUsername(username)).thenReturn(validUser);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(loginAttemptService.isLocked(username)).thenReturn(false);
        doNothing().when(loginAttemptService).resetAttempts(username);

        // Act
        Authentication result = authenticationProvider.authenticate(authentication);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getName());
        assertEquals(validUser, result.getPrincipal());
        verify(loginAttemptService, times(1)).resetAttempts(username);
    }

    @Test
    void authenticate_WithInvalidCredentials_ShouldThrowBadCredentials() {
        // Arrange
        when(userDetailsService.loadUserByUsername(username)).thenReturn(validUser);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);
        doNothing().when(loginAttemptService).loginFailed(username);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authenticationProvider.authenticate(authentication);
        });
        verify(loginAttemptService, times(1)).loginFailed(username);
    }

    @Test
    void authenticate_WithLockedAccount_ShouldThrowLockedException() {
        // Arrange
        when(userDetailsService.loadUserByUsername(username)).thenReturn(validUser);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(loginAttemptService.isLocked(username)).thenReturn(true);

        // Act & Assert
        assertThrows(LockedException.class, () -> {
            authenticationProvider.authenticate(authentication);
        });
        verify(loginAttemptService, never()).resetAttempts(username);
    }

    @Test
    void authenticate_WithNonExistentUser_ShouldThrowBadCredentials() {
        // Arrange
        when(userDetailsService.loadUserByUsername(username)).thenReturn(null);
        doNothing().when(loginAttemptService).loginFailed(username);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authenticationProvider.authenticate(authentication);
        });
        verify(loginAttemptService, times(1)).loginFailed(username);
    }

    @Test
    void authenticate_WithEmptyPassword_ShouldThrowBadCredentials() {
        // Arrange
        Authentication emptyPasswordAuth = new UsernamePasswordAuthenticationToken(username, "");
        when(userDetailsService.loadUserByUsername(username)).thenReturn(validUser);
        doNothing().when(loginAttemptService).loginFailed(username);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authenticationProvider.authenticate(emptyPasswordAuth);
        });
        verify(loginAttemptService, times(1)).loginFailed(username);
    }

    @Test
    void supports_ShouldReturnTrueForUsernamePasswordToken() {
        // Act
        boolean supports = authenticationProvider.supports(UsernamePasswordAuthenticationToken.class);

        // Assert
        assertTrue(supports);
    }

    @Test
    void supports_ShouldReturnFalseForOtherAuthenticationTypes() {
        // Act
        boolean supports = authenticationProvider.supports(Authentication.class);

        // Assert
        assertFalse(supports);
    }

    @Test
    void authenticate_WithEmailInsteadOfUsername_ShouldWork() {
        // Arrange
        String email = "test@example.com";
        Authentication emailAuth = new UsernamePasswordAuthenticationToken(email, password);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(validUser);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(loginAttemptService.isLocked(username)).thenReturn(false);
        doNothing().when(loginAttemptService).resetAttempts(username);

        // Act
        Authentication result = authenticationProvider.authenticate(emailAuth);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getName());
    }
}