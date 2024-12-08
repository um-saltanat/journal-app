package org.example.journal_app.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.journal_app.dto.*;
import org.example.journal_app.dto.auth.AuthLoginDto;
import org.example.journal_app.dto.auth.CustomUserDetails;
import org.example.journal_app.dto.auth.JwtTokenDto;
import org.example.journal_app.dto.auth.RegisterRequest;
import org.example.journal_app.services.JwtService;
import org.example.journal_app.services.TokenService;
import org.example.journal_app.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final TokenService tokenService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        userService.registerUser(registerRequest);
        return ResponseEntity.ok("User created successfully");
    }

    @PostMapping("login")
    public JwtTokenDto login(@RequestBody AuthLoginDto loginDto) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
        );
        if (authenticate.isAuthenticated()) {
            String token = jwtService.GenerateToken(loginDto.getUsername());
            return new JwtTokenDto(token);
        }
        throw new BadCredentialsException("Invalid username or password");
    }

    @PostMapping("/reset_password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        boolean emailExists = userService.checkEmailExists(email);

        if (emailExists) {
            String token = tokenService.generateResetToken(email);
            // In a real-world scenario, you'd send this token via email
            return ResponseEntity.ok("Password reset token: " + token);
        } else {
            return ResponseEntity.status(404).body("Email not found");
        }
    }

    @PostMapping("/update_password")
    public ResponseEntity<String> updatePassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        boolean isValid = tokenService.validateResetToken(resetPasswordDto.getToken());

        if (isValid) {
            boolean isUpdated = userService.updatePassword(resetPasswordDto.getEmail(), resetPasswordDto.getNewPassword());

            if (isUpdated) {
                return ResponseEntity.ok("Password updated successfully");
            } else {
                return ResponseEntity.status(400).body("Error updating the password");
            }
        } else {
            return ResponseEntity.status(400).body("Invalid or expired token");
        }
    }

    @GetMapping
    public Object getCurrentUser(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails userDetail) {
            return new UserDto(userDetail.user());
        }
        return principal;
    }
}
