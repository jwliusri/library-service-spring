package com.jwliusri.library_service.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jwliusri.library_service.audit.Auditable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("api/users")
@Tag(name = "Users", description = "Users CRUD")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get all users")
    public List<UserResponseDto> getAllUsers() {
        return userService.getAllUsers();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public UserResponseDto getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    @Auditable(action = "CREATE_USER", entityType = "USER")
    @Operation(summary = "Create a new user")
    public UserResponseDto createUser(@Valid @RequestBody UserRequestDto request) {
        return userService.createUser(request);
    }

    @PutMapping("/{id}")
    @Auditable(action = "UPDATE_USER", entityType = "USER")
    @Operation(summary = "Update an user")
    public UserResponseDto updateUser(@PathVariable Long id, @Valid @RequestBody UserRequestDto request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @Auditable(action = "DELETE_USER", entityType = "USER")
    @Operation(summary = "Delete an user")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
    
}
