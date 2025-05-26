package com.jwliusri.library_service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTests {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserResponseDto testResponse;
    private UserRequestDto testRequest;

    @BeforeEach
    void setUp() {
        testResponse = new UserResponseDto(
                1L,
                "Test User",
                "testuser",
                "test@example.com",
                RoleEnum.ROLE_VIEWER,
                null,
                null
        );

        testRequest = new UserRequestDto(
                "Test User",
                "testuser",
                "test@example.com",
                "password",
                RoleEnum.ROLE_VIEWER
        );
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        when(userService.getAllUsers()).thenReturn(List.of(testResponse));

        // Act
        List<UserResponseDto> result = userController.getAllUsers();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testResponse, result.get(0));
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getUserById_ShouldReturnUser() {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(testResponse);

        // Act
        UserResponseDto result = userController.getUserById(1L);

        // Assert
        assertEquals(testResponse, result);
        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void getUserById_WhenNotFound_ShouldThrowException() {
        // Arrange
        when(userService.getUserById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userController.getUserById(99L));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void createUser_ShouldReturnCreatedUser() {
        // Arrange
        when(userService.createUser(testRequest)).thenReturn(testResponse);

        // Act
        UserResponseDto result = userController.createUser(testRequest);

        // Assert
        assertEquals(testResponse, result);
        verify(userService, times(1)).createUser(testRequest);
    }

    @Test
    void createUser_WithInvalidInput_ShouldThrowException() {
        // Arrange
        UserRequestDto invalidRequest = new UserRequestDto("", "", "", "", null);
        when(userService.createUser(invalidRequest))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid input"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userController.createUser(invalidRequest));
        
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() {
        // Arrange
        when(userService.updateUser(1L, testRequest)).thenReturn(testResponse);

        // Act
        UserResponseDto result = userController.updateUser(1L, testRequest);

        // Assert
        assertEquals(testResponse, result);
        verify(userService, times(1)).updateUser(1L, testRequest);
    }

    @Test
    void updateUser_WhenNotFound_ShouldThrowException() {
        // Arrange
        when(userService.updateUser(99L, testRequest))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userController.updateUser(99L, testRequest));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deleteUser_ShouldCallService() {
        // Arrange - no return to verify since method is void
        doNothing().when(userService).deleteUser(1L);

        // Act
        userController.deleteUser(1L);

        // Assert
        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void deleteUser_WhenNotFound_ShouldThrowException() {
        // Arrange
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(userService).deleteUser(99L);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userController.deleteUser(99L));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    // @Test
    // void classAnnotations_ShouldBePresent() {
    //     // Verify class-level annotations
    //     RestController restController = UserController.class.getAnnotation(RestController.class);
    //     RequestMapping requestMapping = UserController.class.getAnnotation(RequestMapping.class);
    //     Tag tag = UserController.class.getAnnotation(Tag.class);
    //     SecurityRequirement securityRequirement = UserController.class.getAnnotation(SecurityRequirement.class);

    //     assertNotNull(restController);
    //     assertEquals("api/users", requestMapping.value()[0]);
    //     assertEquals("Users", tag.name());
    //     assertEquals("Users CRUD", tag.description());
    //     assertEquals("bearerAuth", securityRequirement.name());
    // }

    // @Test
    // void methodAnnotations_ShouldBePresent() throws NoSuchMethodException {
    //     // Verify method-level annotations
    //     assertNotNull(UserController.class.getMethod("createUser", UserRequestDto.class)
    //             .getAnnotation(Auditable.class));
    //     assertNotNull(UserController.class.getMethod("updateUser", Long.class, UserRequestDto.class)
    //             .getAnnotation(Auditable.class));
    //     assertNotNull(UserController.class.getMethod("deleteUser", Long.class)
    //             .getAnnotation(Auditable.class));
        
    //     // Verify Operation annotations
    //     assertNotNull(UserController.class.getMethod("getAllUsers")
    //             .getAnnotation(Operation.class));
    //     assertNotNull(UserController.class.getMethod("getUserById", Long.class)
    //             .getAnnotation(Operation.class));
    // }
}