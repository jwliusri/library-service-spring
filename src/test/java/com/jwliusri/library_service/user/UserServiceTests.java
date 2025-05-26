package com.jwliusri.library_service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRequestDto testRequest;
    private UserResponseDto testResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .fullName("Test User")
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(RoleEnum.ROLE_VIEWER)
                .build();

        testRequest = new UserRequestDto(
                "Test User",
                "testuser",
                "test@example.com",
                "password",
                RoleEnum.ROLE_VIEWER
        );

        testResponse = new UserResponseDto(
                1L,
                "Test User",
                "testuser",
                "test@example.com",
                RoleEnum.ROLE_VIEWER,
                null,
                null
        );
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        // Act
        List<UserResponseDto> result = userService.getAllUsers();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testResponse.getFullName(), result.get(0).getFullName());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        UserResponseDto result = userService.getUserById(1L);

        // Assert
        assertEquals(testResponse, result);
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_WhenNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.getUserById(99L));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void getUserByUsername_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserByUsername("testuser");

        // Assert
        assertEquals(testUser, result);
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void getUserByUsername_WhenNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.getUserByUsername("unknown"));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void createUser_ShouldCreateAndReturnUser() {
        // Arrange
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponseDto result = userService.createUser(testRequest);

        // Assert
        assertEquals(testResponse, result);
        verify(passwordEncoder, times(1)).encode("password");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_ShouldUpdateAndReturnUser() {
        // Arrange
        UserRequestDto updateRequest = new UserRequestDto(
                "Updated User",
                "updateduser",
                "updated@example.com",
                "newpassword",
                RoleEnum.ROLE_EDITOR
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setFullName(updateRequest.getFullName());
            user.setUsername(updateRequest.getUsername());
            user.setEmail(updateRequest.getEmail());
            user.setPassword("newEncodedPassword");
            user.setRole(updateRequest.getRole());
            return user;
        });

        // Act
        UserResponseDto result = userService.updateUser(1L, updateRequest);

        // Assert
        assertEquals("Updated User", result.getFullName());
        assertEquals("updateduser", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals(RoleEnum.ROLE_EDITOR, result.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_WithoutPassword_ShouldNotUpdatePassword() {
        // Arrange
        UserRequestDto updateRequest = new UserRequestDto(
                "Updated User",
                "updateduser",
                "updated@example.com",
                null,
                RoleEnum.ROLE_EDITOR
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setFullName(updateRequest.getFullName());
            user.setUsername(updateRequest.getUsername());
            user.setEmail(updateRequest.getEmail());
            user.setRole(updateRequest.getRole());
            return user;
        });

        // Act
        UserResponseDto result = userService.updateUser(1L, updateRequest);

        // Assert
        assertEquals("Updated User", result.getFullName());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateUser_WithoutRole_ShouldNotUpdateRole() {
        // Arrange
        UserRequestDto updateRequest = new UserRequestDto(
                "Updated User",
                "updateduser",
                "updated@example.com",
                null,
                null
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setFullName(updateRequest.getFullName());
            user.setUsername(updateRequest.getUsername());
            user.setEmail(updateRequest.getEmail());
            return user;
        });

        // Act
        UserResponseDto result = userService.updateUser(1L, updateRequest);

        // Assert
        assertEquals("Updated User", result.getFullName());
        assertEquals(testUser.getRole(), result.getRole());
    }

    @Test
    void updateUser_WhenNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.updateUser(99L, testRequest));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void deleteUser_ShouldDeleteUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    void deleteUser_WhenNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.deleteUser(99L));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void getAuthUser_ShouldReturnUser() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getAuthUser(authentication);

        // Assert
        assertEquals(testUser, result);
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void getAuthUser_WhenNotFound_ShouldThrowException() {
        // Arrange
        when(authentication.getName()).thenReturn("unknown");
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.getAuthUser(authentication));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void mapToResponse_ShouldConvertUserToResponseDto() {
        // Act
        UserResponseDto result = userService.mapToResponse(testUser);

        // Assert
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getFullName(), result.getFullName());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getRole(), result.getRole());
    }
}