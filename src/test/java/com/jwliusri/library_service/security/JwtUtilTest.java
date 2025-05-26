package com.jwliusri.library_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private SecretKey testKey;
    private final String testUsername = "testuser";
    private String validToken;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", "testSecretKeyWithAtLeast32CharactersLong123");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 3600000); // 1 hour
        
        // Initialize the key manually since @PostConstruct won't work in tests
        testKey = Keys.hmacShaKeyFor("testSecretKeyWithAtLeast32CharactersLong123"
                .getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(jwtUtil, "key", testKey);
        
        // Generate a valid token for testing
        validToken = Jwts.builder()
                .setSubject(testUsername)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)))
                .signWith(testKey, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void generateToken_ShouldReturnValidJwtToken() {
        // Act
        String token = jwtUtil.generateToken(testUsername);

        // Assert
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3); // Valid JWT has 3 parts
    }

    @Test
    void getUsernameFromToken_ShouldReturnCorrectUsername() {
        // Act
        String username = jwtUtil.getUsernameFromToken(validToken);

        // Assert
        assertEquals(testUsername, username);
    }

    @Test
    void getUsernameFromToken_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.token.string";

        // Act & Assert
        assertThrows(MalformedJwtException.class, () -> jwtUtil.getUsernameFromToken(invalidToken));
    }

    @Test
    void validateJwtToken_WithValidToken_ShouldReturnTrue() {
        // Act
        boolean isValid = jwtUtil.validateJwtToken(validToken);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateJwtToken_WithExpiredToken_ShouldReturnFalse() throws Exception {
        // Arrange
        String expiredToken = Jwts.builder()
                .setSubject(testUsername)
                .setIssuedAt(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)))
                .setExpiration(new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)))
                .signWith(testKey, SignatureAlgorithm.HS256)
                .compact();

        // Act
        boolean isValid = jwtUtil.validateJwtToken(expiredToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_WithMalformedToken_ShouldReturnFalse() {
        // Arrange
        String malformedToken = "malformed.token";

        // Act
        boolean isValid = jwtUtil.validateJwtToken(malformedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_WithInvalidSignature_ShouldReturnFalse() throws Exception {
        // Arrange
        SecretKey differentKey = Keys.hmacShaKeyFor("differentSecretKeyWithAtLeast32Characters123"
                .getBytes(StandardCharsets.UTF_8));
        String tamperedToken = Jwts.builder()
                .setSubject(testUsername)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)))
                .signWith(differentKey, SignatureAlgorithm.HS256)
                .compact();

        // Act
        boolean isValid = jwtUtil.validateJwtToken(tamperedToken);

        // Assert
        assertFalse(isValid);
    }

    // @Test
    // void validateJwtToken_WithUnsupportedToken_ShouldReturnFalse() throws Exception {
    //     // Arrange
    //     JwtParserBuilder mockParserBuilder = mock(JwtParserBuilder.class);
    //     JwtParser mockParser = mock(JwtParser.class);
    //     when(mockParserBuilder.setSigningKey(any(byte[].class))).thenReturn(mockParserBuilder);
    //     when(mockParserBuilder.build()).thenReturn(mockParser);
    //     when(mockParser.parseClaimsJws(any(String.class))).thenThrow(UnsupportedJwtException.class);

    //     // Create a spy to override parser behavior
    //     JwtUtil jwtUtilSpy = spy(jwtUtil);
    //     when(jwtUtilSpy.getParserBuilder()).thenReturn(mockParserBuilder);

    //     // Act
    //     boolean isValid = jwtUtilSpy.validateJwtToken("unsupported.token");

    //     // Assert
    //     assertFalse(isValid);
    // }

    // @Test
    // void validateJwtToken_WithEmptyClaims_ShouldReturnFalse() throws Exception {
    //     // Arrange
    //     JwtParserBuilder mockParserBuilder = mock(JwtParserBuilder.class);
    //     JwtParser mockParser = mock(JwtParser.class);
    //     when(mockParserBuilder.setSigningKey(any(byte[].class))).thenReturn(mockParserBuilder);
    //     when(mockParserBuilder.build()).thenReturn(mockParser);
    //     when(mockParser.parseClaimsJws(any(String.class))).thenThrow(IllegalArgumentException.class);

    //     // Create a spy to override parser behavior
    //     JwtUtil jwtUtilSpy = spy(jwtUtil);
    //     when(jwtUtilSpy.getParserBuilder()).thenReturn(mockParserBuilder);

    //     // Act
    //     boolean isValid = jwtUtilSpy.validateJwtToken("empty.claims.token");

    //     // Assert
    //     assertFalse(isValid);
    // }

    @Test
    void init_ShouldSetKeyProperly() {
        // Arrange
        JwtUtil newJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(newJwtUtil, "jwtSecret", "newTestSecretKeyWithAtLeast32Characters456");

        // Act
        newJwtUtil.init();

        // Assert
        SecretKey key = (SecretKey) ReflectionTestUtils.getField(newJwtUtil, "key");
        assertNotNull(key);
    }

}