package com.jwliusri.library_service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import com.jwliusri.library_service.user.RoleEnum;
import com.jwliusri.library_service.user.User;
import com.jwliusri.library_service.user.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {
    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService customUserDetailsService;
    
    private User testUser;

    @BeforeEach
    public void setUp() throws Exception {
        customUserDetailsService = new CustomUserDetailsService(userRepository);

        testUser = User.builder()
                .id(1L)
                .fullName("Test User")
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(RoleEnum.ROLE_VIEWER)
                .build();
    }

    @Test
    public void GIVEN_username_THEN_return_user_details() {
        //Arrange
        final String username = "testuser";
        when(userRepository.findByUsernameOrEmail(username, username)).thenReturn(Optional.of(testUser));

        //Act
        final UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        //Assert
        assertNotNull(userDetails);
        assertEquals(testUser.getUsername(), ReflectionTestUtils.getField(userDetails, "username"));
    }
}