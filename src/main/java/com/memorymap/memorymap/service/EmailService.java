package com.memorymap.memorymap.service;

import com.memorymap.memorymap.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

// Uses Brevo's HTTPS transactional email API rather than raw SMTP: Render's free
// tier blocks outbound SMTP (port 587) to prevent spam-relay abuse, which made
// every send silently time out. Plain HTTPS (443) isn't blocked — it's the same
// kind of call this app already makes successfully to Cloudinary and Photon.
@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final RestClient restClient = RestClient.create();

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.brevo.api-key}")
    private String brevoApiKey;

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

    // Email delivery is best-effort: a transient failure here shouldn't block
    // registration or a password-reset request from otherwise succeeding.
    private void send(String to, String subject, String text) {
        try {
            Map<String, Object> body = Map.of(
                    "sender", Map.of("name", "MemoryMap", "email", fromAddress),
                    "to", List.of(Map.of("email", to)),
                    "subject", subject,
                    "textContent", text
            );

            restClient.post()
                    .uri("https://api.brevo.com/v3/smtp/email")
                    .header("api-key", brevoApiKey)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
