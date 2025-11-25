package com.techup.spring_project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.from:no-reply@travel-app.local}")
    private String fromAddress;
    
    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;
    
    public void sendEmailVerification(String recipientEmail, String displayName, String verificationLink) {
        if (!mailEnabled) {
            log.info("Mail disabled via configuration. Skipping verification mail to {}", recipientEmail);
            return;
        }
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setFrom(fromAddress);
        message.setSubject("Verify your Travel App account");
        
        String name = displayName != null && !displayName.isBlank() ? displayName : "there";
        message.setText("""
                Hi %s,
                
                Thanks for registering with Travel App. Please verify your email address by clicking the link below:
                
                %s
                
                If you did not sign up, please ignore this email.
                """.formatted(name, verificationLink));
        
        try {
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send verification email to {}: {}", recipientEmail, ex.getMessage(), ex);
            throw ex;
        }
    }
}

