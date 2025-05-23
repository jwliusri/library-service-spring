package com.jwliusri.library_service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String usernameOrEmail = authentication.getName();
        String password = (String) authentication.getCredentials();

        UserDetails user = userDetailsService.loadUserByUsername(usernameOrEmail);

        // Verify user credentials
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            try {
                loginAttemptService.loginFailed(usernameOrEmail); // Record failed login attempt
            } catch (Exception e) {
                // usernameOrEmail not found
            }
            throw new BadCredentialsException("Invalid credential.");
        }

        // Check if the user is blocked due to too many failed login attempts
        if (loginAttemptService.isLocked(user.getUsername())) {
            throw new LockedException("You have been temporarily locked due to too many failed login attempts.");
        }

        loginAttemptService.resetAttempts(user.getUsername()); // Record successful login

        return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
         return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
