package com.jwliusri.library_service.security.auth;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.jwliusri.library_service.audit.Auditable;
import com.jwliusri.library_service.security.JwtUtil;
import com.jwliusri.library_service.security.mfa.MfaOtpService;
import com.jwliusri.library_service.user.User;
import com.jwliusri.library_service.user.UserRequest;
import com.jwliusri.library_service.user.UserResponse;
import com.jwliusri.library_service.user.UserService;
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
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsernameOrEmail(),
                    request.getPassword()
                )
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.getAuthUser(authentication);

            return new LoginResponse(user.getId(), mfaOtpService.generateOtp(userDetails.getUsername()));
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
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        UserRequest userRequest = UserRequest.builder()
            .fullName(request.getFullName())
            .username(request.getUsername())
            .email(request.getEmail())
            .password(request.getPassword())
            .build();

       return userService.createUser(userRequest);
    }

    @GetMapping("me")
    @PreAuthorize("isAuthenticated()")
    public String getMe(Authentication auth) {
        return auth.getName();
    }
    
}
