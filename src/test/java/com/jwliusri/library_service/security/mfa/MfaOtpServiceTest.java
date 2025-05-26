package com.jwliusri.library_service.security.mfa;

import com.jwliusri.library_service.email.EmailService;
import com.jwliusri.library_service.user.User;
import com.jwliusri.library_service.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MfaOtpServiceTest {

    @Mock
    private MfaOtpRepository mfaOtpRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MfaOtpService mfaOtpService;

    private final String testUsername = "testuser";
    private final String testEmail = "test@example.com";
    private final String testRequestId = UUID.randomUUID().toString();
    private final int testOtp = 123456;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername(testUsername);
        testUser.setEmail(testEmail);
        
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
    }

    @Test
    void generateOtp_ShouldCreateOtpRecordAndSendEmail() {
        // Arrange
        MfaOtp expectedOtp = new MfaOtp(testRequestId, testUsername, testOtp);
        when(mfaOtpRepository.save(any(MfaOtp.class))).thenReturn(expectedOtp);

        // Act
        String requestId = mfaOtpService.generateOtp(testUsername);

        // Assert
        assertNotNull(requestId);
        verify(mfaOtpRepository).save(any(MfaOtp.class));
        verify(emailService).sendSimpleMail(argThat(email -> 
            email.getRecipient().equals(testEmail) &&
            email.getMsgBody().contains(String.valueOf(expectedOtp.getOtp()))
        ));
    }

    @Test
    void generateOtp_WithInvalidUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("invaliduser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> 
            mfaOtpService.generateOtp("invaliduser")
        );
    }

    @Test
    void validateOtp_WithValidOtp_ShouldReturnTrue() {
        // Arrange
        MfaOtp storedOtp = new MfaOtp(testRequestId, testUsername, testOtp);
        when(mfaOtpRepository.findById(testRequestId)).thenReturn(Optional.of(storedOtp));

        // Act
        boolean isValid = mfaOtpService.validateOtp(testRequestId, testOtp);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateOtp_WithInvalidOtp_ShouldReturnFalse() {
        // Arrange
        MfaOtp storedOtp = new MfaOtp(testRequestId, testUsername, testOtp);
        when(mfaOtpRepository.findById(testRequestId)).thenReturn(Optional.of(storedOtp));

        // Act
        boolean isValid = mfaOtpService.validateOtp(testRequestId, 654321);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateOtp_WithInvalidRequestId_ShouldThrowException() {
        // Arrange
        when(mfaOtpRepository.findById("invalid-request")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> 
            mfaOtpService.validateOtp("invalid-request", testOtp)
        );
    }

    @Test
    void getUsernameFromOtp_ShouldReturnUsername() {
        // Arrange
        MfaOtp storedOtp = new MfaOtp(testRequestId, testUsername, testOtp);
        when(mfaOtpRepository.findById(testRequestId)).thenReturn(Optional.of(storedOtp));

        // Act
        String username = mfaOtpService.getUsernameFromOtp(testRequestId);

        // Assert
        assertEquals(testUsername, username);
    }

    @Test
    void getUsernameFromOtp_WithInvalidRequestId_ShouldThrowException() {
        // Arrange
        when(mfaOtpRepository.findById("invalid-request")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> 
            mfaOtpService.getUsernameFromOtp("invalid-request")
        );
    }

    @Test
    void generateOtp_ShouldGenerateDifferentRequestIds() {
        // Arrange
        MfaOtp otp1 = new MfaOtp(UUID.randomUUID().toString(), testUsername, testOtp);
        MfaOtp otp2 = new MfaOtp(UUID.randomUUID().toString(), testUsername, testOtp);
        when(mfaOtpRepository.save(any(MfaOtp.class))).thenReturn(otp1).thenReturn(otp2);

        // Act
        String requestId1 = mfaOtpService.generateOtp(testUsername);
        String requestId2 = mfaOtpService.generateOtp(testUsername);

        // Assert
        assertNotEquals(requestId1, requestId2);
    }
}