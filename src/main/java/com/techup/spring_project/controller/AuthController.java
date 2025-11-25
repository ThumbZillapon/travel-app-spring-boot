package com.techup.spring_project.controller;

import com.techup.spring_project.dto.ApiMessageResponse;
import com.techup.spring_project.dto.AuthResponse;
import com.techup.spring_project.dto.LoginRequest;
import com.techup.spring_project.dto.RegisterRequest;
import com.techup.spring_project.dto.UserDto;
import com.techup.spring_project.dto.VerifyEmailRequest;
import com.techup.spring_project.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiMessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        ApiMessageResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        UserDto user = authService.getCurrentUser(username);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/verify-email")
    public ResponseEntity<ApiMessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        ApiMessageResponse response = authService.verifyEmail(request.getToken());
        return ResponseEntity.ok(response);
    }
}
