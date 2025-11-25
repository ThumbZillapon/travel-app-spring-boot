package com.techup.spring_project.service;

import com.techup.spring_project.dto.ApiMessageResponse;
import com.techup.spring_project.dto.AuthResponse;
import com.techup.spring_project.dto.LoginRequest;
import com.techup.spring_project.dto.RegisterRequest;
import com.techup.spring_project.dto.UserDto;
import com.techup.spring_project.entity.User;
import com.techup.spring_project.exception.DuplicateEmailException;
import com.techup.spring_project.exception.ForbiddenException;
import com.techup.spring_project.exception.ResourceNotFoundException;
import com.techup.spring_project.repository.UserRepository;
import com.techup.spring_project.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final MailService mailService;
    
    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;
    
    @Value("${app.verification-token-expiration-hours:24}")
    private long verificationTokenExpirationHours;
    
    @Transactional
    public ApiMessageResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("This email is already registered");
        }
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());
        
        user.setEmailVerified(false);
        user.setVerificationToken(generateVerificationToken());
        user.setVerificationTokenExpiresAt(
                OffsetDateTime.now().plusHours(verificationTokenExpirationHours)
        );
        
        User savedUser = userRepository.save(user);
        
        String verificationLink = buildVerificationLink(savedUser.getVerificationToken());
        mailService.sendEmailVerification(savedUser.getEmail(), savedUser.getDisplayName(), verificationLink);
        
        return new ApiMessageResponse("Account created. Please check your email to verify before logging in.");
    }
    
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (!user.isEmailVerified()) {
            throw new ForbiddenException("Please verify your email before logging in.");
        }
        
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getId());
        
        UserDto userDto = convertToDto(user);
        return new AuthResponse(token, userDto);
    }
    
    @Transactional
    public ApiMessageResponse verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Verification token is required");
        }
        
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification link."));
        
        if (user.isEmailVerified()) {
            return new ApiMessageResponse("Email already verified. You can log in.");
        }
        
        if (user.getVerificationTokenExpiresAt() != null &&
                user.getVerificationTokenExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Verification link has expired. Please register again.");
        }
        
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiresAt(null);
        userRepository.save(user);
        
        return new ApiMessageResponse("Email verified successfully. You can now log in.");
    }
    
    public UserDto getCurrentUser(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToDto(user);
    }
    
    private UserDto convertToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getCreatedAt(),
                user.isEmailVerified()
        );
    }
    
    private String generateVerificationToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    private String buildVerificationLink(String token) {
        String base = frontendBaseUrl != null ? frontendBaseUrl : "http://localhost:5173";
        return base.endsWith("/")
                ? base + "verify-email?token=" + token
                : base + "/verify-email?token=" + token;
    }
}

