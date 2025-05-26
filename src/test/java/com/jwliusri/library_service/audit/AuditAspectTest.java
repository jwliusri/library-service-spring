package com.jwliusri.library_service.audit;

import com.jwliusri.library_service.article.ArticleResponseDto;
import com.jwliusri.library_service.security.auth.LoginResponseDto;
import com.jwliusri.library_service.security.auth.ValidateResponseDto;
import com.jwliusri.library_service.user.UserResponseDto;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuditAspectTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ServletRequestAttributes requestAttributes;

    @InjectMocks
    private AuditAspect auditAspect;

    private final String testUsername = "testuser";
    private final String userAgent = "Test User Agent";
    private final String ipAddress = "127.0.0.1";

    @BeforeEach
    void setUp() throws Exception {
        // Setup security context
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).thenReturn(true);
        when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(testUsername);

        // Mock request context
        when(request.getHeader("User-Agent")).thenReturn(userAgent);
        when(request.getRemoteAddr()).thenReturn(ipAddress);
        when(requestAttributes.getRequest()).thenReturn(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);
    }

    @Test
    void auditSuccessfulOperation_ShouldCreateSuccessLog() throws Exception {
        // Arrange
        Auditable auditable = createTestAuditable();
        ArticleResponseDto response = new ArticleResponseDto(1L, null, null, null, null, false, null, null);
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        
        // Act
        auditAspect.auditSuccessfulOperation(joinPoint, auditable, response);

        // Assert
        verify(auditLogRepository).save(argThat(log ->
            log.getUsername().equals(testUsername) &&
            log.getUserAgent().equals(userAgent) &&
            log.getIpAddress().equals(ipAddress) &&
            log.isSuccess() &&
            log.getErrorMessage() == null &&
            log.getEntityId() == 1L
        ));
    }

    @Test
    void auditFailedOperation_ShouldCreateErrorLog() throws Exception {
        // Arrange
        Auditable auditable = createTestAuditable();
        when(joinPoint.getArgs()).thenReturn(new Object[]{2L}); // Simulate ID in arguments
        Exception ex = new RuntimeException("Test error");

        // Act
        auditAspect.auditFailedOperation(joinPoint, auditable, ex);

        // Assert
        verify(auditLogRepository).save(argThat(log ->
            !log.isSuccess() &&
            log.getErrorMessage().equals("Test error") &&
            log.getEntityId() == 2L
        ));
    }

    @Test
    void createAuditLog_WithAnonymousUser_ShouldUseAnonymousName() throws Exception {
        // Arrange
        SecurityContextHolder.clearContext();
        Auditable auditable = createTestAuditable();
        when(joinPoint.getArgs()).thenReturn(new Object[]{});

        // Act
        auditAspect.auditSuccessfulOperation(joinPoint, auditable, null);

        // Assert
        verify(auditLogRepository).save(argThat(log ->
            log.getUsername().equals("ANONYMOUS")
        ));
    }

    @Test
    void extractEntityId_FromResponseDto_ShouldReturnId() {
        // Test with ArticleResponseDto
        ArticleResponseDto articleResponse = new ArticleResponseDto(10L, null, null, null, null, false, null, null);
        Optional<Long> result = auditAspect.extractEntityId(joinPoint, null, articleResponse);
        assertEquals(Optional.of(10L), result);

        // Test with UserResponseDto
        UserResponseDto userResponse = new UserResponseDto(20L, null, null, null, null, null, null);
        result = auditAspect.extractEntityId(joinPoint, null, userResponse);
        assertEquals(Optional.of(20L), result);

        // Test with LoginResponseDto
        LoginResponseDto loginResponse = new LoginResponseDto(30L, null);
        result = auditAspect.extractEntityId(joinPoint, null, loginResponse);
        assertEquals(Optional.of(30L), result);

        // Test with ValidateResponseDto
        ValidateResponseDto validateResponse = new ValidateResponseDto(40L, null);
        result = auditAspect.extractEntityId(joinPoint, null, validateResponse);
        assertEquals(Optional.of(40L), result);
    }

    @Test
    void extractEntityId_FromMethodArguments_ShouldReturnId() throws Exception {
        // Arrange
        Object[] args = {50L}; // Long argument
        when(joinPoint.getArgs()).thenReturn(args);

        // Act
        Optional<Long> result = auditAspect.extractEntityId(joinPoint, null, null);

        // Assert
        assertEquals(Optional.of(50L), result);
    }

    @Test
    void extractEntityId_FromAnnotatedEntity_ShouldReturnId() throws Exception {
        // Arrange
        TestEntity entity = new TestEntity(60L);
        Object[] args = {entity};
        when(joinPoint.getArgs()).thenReturn(args);

        // Act
        Optional<Long> result = auditAspect.extractEntityId(joinPoint, null, null);

        // Assert
        assertEquals(Optional.of(60L), result);
    }

    @Test
    void extractEntityId_WithNoIdAvailable_ShouldReturnEmpty() {
        // Arrange
        when(joinPoint.getArgs()).thenReturn(new Object[]{"string without id"});

        // Act
        Optional<Long> result = auditAspect.extractEntityId(joinPoint, null, null);

        // Assert
        assertEquals(Optional.empty(), result);
    }

    @Test
    void getCurrentUsername_WhenAuthenticated_ShouldReturnUsername() {
        // Arrange
        Authentication auth = new UsernamePasswordAuthenticationToken(testUsername, "password");
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Act
        String username = auditAspect.getCurrentUsername();

        // Assert
        assertEquals(testUsername, username);
    }

    @Test
    void getCurrentUsername_WhenNotAuthenticated_ShouldReturnAnonymous() {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act
        String username = auditAspect.getCurrentUsername();

        // Assert
        assertEquals("ANONYMOUS", username);
    }

    // Helper method to create test Auditable annotation
    private Auditable createTestAuditable() throws Exception {
        Method method = this.getClass().getMethod("testMethod");
        return method.getAnnotation(Auditable.class);
    }

    @Auditable(action = "TEST_ACTION", entityType = "TEST_ENTITY")
    public void testMethod() {
        // Dummy method for annotation testing
    }

    // Test entity class with @AuditEntity annotation
    @AuditEntity
    private static class TestEntity {
        private final Long id;

        public TestEntity(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }
}