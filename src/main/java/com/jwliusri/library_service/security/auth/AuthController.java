package com.jwliusri.library_service.security.auth;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.jwliusri.library_service.audit.Auditable;
import com.jwliusri.library_service.security.JwtUtil;
import com.jwliusri.library_service.security.mfa.MfaOtpService;
import com.jwliusri.library_service.user.User;
import com.jwliusri.library_service.user.UserRequestDto;
import com.jwliusri.library_service.user.UserResponseDto;
import com.jwliusri.library_service.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("api/auth")
@Tag(name = "_Auth", description = "Authentication operations")
public class AuthController {

    private final MfaOtpService mfaOtpService;

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    AuthController(JwtUtil jwtUtil, AuthenticationManager authenticationManager, UserService userService, MfaOtpService mfaOtpService) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.mfaOtpService = mfaOtpService;
    }

    @PostMapping("login")
    @Auditable(action = "USER_LOGIN", entityType = "AUTH")
    @Operation(summary = "Login", description = "Login with username or email, successfull login will return a MFA OTP requestId and an email is containing the OTP code is sent to the user's email.")
    public LoginResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsernameOrEmail(),
                    request.getPassword()
                )
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.getAuthUser(authentication);

            return new LoginResponseDto(user.getId(), mfaOtpService.generateOtp(userDetails.getUsername()));
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (LockedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw e;
        }
    }

    @PostMapping("validate")
    @Auditable(action = "USER_VALIDATE", entityType = "AUTH")
    @Operation(summary = "Validate MFA OTP ", description = "Validate MFA OTP, successfull validation will return JWT token.")
    public ValidateResponseDto validate(@Valid @RequestBody ValidateRequestDto request) {
        try {
            if (mfaOtpService.validateOtp(request.getRequestId(), request.getOtp())) {
                String username = mfaOtpService.getUsernameFromOtp(request.getRequestId());
                User user = userService.getUserByUsername(username);
                return new ValidateResponseDto(user.getId(), jwtUtil.generateToken(username));
            }
        } catch (NoSuchElementException e) {
        } catch (Exception e) {
            throw e;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid requestId or otp");
    }


    @PostMapping("register")
    @Auditable(action = "USER_REGISTER", entityType = "USER")
    @Operation(summary = "Register new user ")
    public UserResponseDto register(@Valid @RequestBody RegisterRequestDto request) {
        UserRequestDto userRequest = UserRequestDto.builder()
            .fullName(request.getFullName())
            .username(request.getUsername())
            .email(request.getEmail())
            .password(request.getPassword())
            .build();

       return userService.createUser(userRequest);
    }

    @GetMapping("me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get JWT token username")
    public String getMe(Authentication auth) {
        return auth.getName();
    }
    
}
