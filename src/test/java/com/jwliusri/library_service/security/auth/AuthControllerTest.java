package com.jwliusri.library_service.security.auth;

import com.jwliusri.library_service.security.JwtUtil;
import com.jwliusri.library_service.security.mfa.MfaOtpService;
import com.jwliusri.library_service.user.User;
import com.jwliusri.library_service.user.UserRequestDto;
import com.jwliusri.library_service.user.UserResponseDto;
import com.jwliusri.library_service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.NoSuchElementException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthControllerTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private MfaOtpService mfaOtpService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private final String testUsername = "testuser";
    private final String testPassword = "password";
    private final String testEmail = "test@example.com";
    private final String testFullName = "Test User";
    private final Long testUserId = 1L;
    private final String testToken = "test.jwt.token";
    private final String testRequestId = "test-request-id";
    private final int testOtp = 123456;

    @BeforeEach
    void setUp() {
        // Common setup for authentication
        authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(testUsername);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnLoginResponse() {
        // Arrange
        LoginRequestDto request = new LoginRequestDto(testUsername, testPassword);
        User testUser = new User();
        testUser.setId(testUserId);

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);
        when(userService.getAuthUser(any())).thenReturn(testUser);
        when(mfaOtpService.generateOtp(testUsername)).thenReturn(testRequestId);

        // Act
        LoginResponseDto response = authController.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(testUserId, response.getId());
        assertEquals(testRequestId, response.getRequestId());
        verify(authenticationManager).authenticate(any());
        verify(mfaOtpService).generateOtp(testUsername);
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowBadRequest() {
        // Arrange
        LoginRequestDto request = new LoginRequestDto(testUsername, "wrongpassword");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authController.login(request));
        
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Bad credentials", exception.getReason());
    }

    @Test
    void login_WithLockedAccount_ShouldThrowForbidden() {
        // Arrange
        LoginRequestDto request = new LoginRequestDto(testUsername, testPassword);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new LockedException("Account locked"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authController.login(request));
        
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Account locked", exception.getReason());
    }

    @Test
    void validate_WithValidOtp_ShouldReturnValidateResponse() {
        // Arrange
        ValidateRequestDto request = new ValidateRequestDto(testRequestId, testOtp);
        User testUser = new User();
        testUser.setId(testUserId);

        when(mfaOtpService.validateOtp(testRequestId, testOtp)).thenReturn(true);
        when(mfaOtpService.getUsernameFromOtp(testRequestId)).thenReturn(testUsername);
        when(userService.getUserByUsername(testUsername)).thenReturn(testUser);
        when(jwtUtil.generateToken(testUsername)).thenReturn(testToken);

        // Act
        ValidateResponseDto response = authController.validate(request);

        // Assert
        assertNotNull(response);
        assertEquals(testUserId, response.getId());
        assertEquals(testToken, response.getToken());
        verify(mfaOtpService).validateOtp(testRequestId, testOtp);
        verify(jwtUtil).generateToken(testUsername);
    }

    @Test
    void validate_WithInvalidOtp_ShouldThrowBadRequest() {
        // Arrange
        ValidateRequestDto request = new ValidateRequestDto(testRequestId, 999999);
        when(mfaOtpService.validateOtp(testRequestId, 999999)).thenReturn(false);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authController.validate(request));
        
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid requestId or otp", exception.getReason());
    }

    @Test
    void validate_WithInvalidRequestId_ShouldThrowBadRequest() {
        // Arrange
        ValidateRequestDto request = new ValidateRequestDto("invalid-request", testOtp);
        when(mfaOtpService.validateOtp("invalid-request", testOtp))
                .thenThrow(new NoSuchElementException());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authController.validate(request));
        
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid requestId or otp", exception.getReason());
    }

    @Test
    void register_WithValidRequest_ShouldReturnUserResponse() {
        // Arrange
        RegisterRequestDto request = new RegisterRequestDto(testFullName, testUsername, testEmail, testPassword);
        UserRequestDto userRequest = UserRequestDto.builder()
                .fullName(testFullName)
                .username(testUsername)
                .email(testEmail)
                .password(testPassword)
                .build();
        
        UserResponseDto expectedResponse = new UserResponseDto(testUserId, testFullName, testUsername, testEmail, null, null, null);
        when(userService.createUser(userRequest)).thenReturn(expectedResponse);

        // Act
        UserResponseDto response = authController.register(request);

        // Assert
        assertNotNull(response);
        assertEquals(testUserId, response.getId());
        assertEquals(testFullName, response.getFullName());
        assertEquals(testUsername, response.getUsername());
        assertEquals(testEmail, response.getEmail());
        verify(userService).createUser(userRequest);
    }

    @Test
    void getMe_WhenAuthenticated_ShouldReturnUsername() {
        // Arrange
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(testUsername);

        // Act
        String response = authController.getMe(auth);

        // Assert
        assertEquals(testUsername, response);
    }
}