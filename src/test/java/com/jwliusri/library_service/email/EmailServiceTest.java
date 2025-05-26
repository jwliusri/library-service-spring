package com.jwliusri.library_service.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailService emailService;

    private final String testFromAddress = "noreply@library.com";
    private final String testRecipient = "user@example.com";
    private final String testSubject = "Test Subject";
    private final String testMessage = "Test Message Body";

    @BeforeEach
    void setUp() {
        // Set up test properties
        ReflectionTestUtils.setField(emailService, "fromAddress", testFromAddress);
    }

    @Test
    void sendSimpleMail_ShouldSendEmailWithCorrectDetails() {
        // Arrange
        EmailDetail detail = new EmailDetail();
        detail.setRecipient(testRecipient);
        detail.setSubject(testSubject);
        detail.setMsgBody(testMessage);

        // Act
        emailService.sendSimpleMail(detail);

        // Assert
        verify(javaMailSender).send(argThat((SimpleMailMessage message) -> 
            message.getFrom().equals(testFromAddress) &&
            message.getTo()[0].equals(testRecipient) &&
            message.getSubject().equals(testSubject) &&
            message.getText().equals(testMessage)
        ));
    }

    @Test
    void sendSimpleMail_WhenMailSenderThrowsException_ShouldPropagate() {
        // Arrange
        EmailDetail detail = new EmailDetail();
        detail.setRecipient(testRecipient);
        detail.setSubject(testSubject);
        detail.setMsgBody(testMessage);

        doThrow(new RuntimeException("Mail sending failed")).when(javaMailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> emailService.sendSimpleMail(detail));
    }

    @Test
    void sendSimpleMail_WithNullDetail_ShouldThrowException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> emailService.sendSimpleMail(null));
    }
}