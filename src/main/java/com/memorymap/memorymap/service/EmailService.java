package com.memorymap.memorymap.service;

import com.memorymap.memorymap.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(User user) {
        String link = frontendUrl + "/verify-email?token=" + user.getVerificationToken();
        send(user.getEmail(), "Verify your MemoryMap email",
                "Welcome to MemoryMap!\n\nClick the link below to verify your email address:\n\n"
                        + link + "\n\nThis link expires in 24 hours.");
    }

    public void sendPasswordResetEmail(User user) {
        String link = frontendUrl + "/reset-password?token=" + user.getResetToken();
        send(user.getEmail(), "Reset your MemoryMap password",
                "Click the link below to reset your MemoryMap password:\n\n" + link
                        + "\n\nThis link expires in 30 minutes. If you didn't request this, you can ignore this email.");
    }

    // Email delivery is best-effort: a transient SMTP failure here shouldn't block
    // registration or a password-reset request from otherwise succeeding.
    private void send(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("MemoryMap <" + fromAddress + ">");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
