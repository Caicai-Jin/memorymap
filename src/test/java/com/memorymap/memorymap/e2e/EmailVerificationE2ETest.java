package com.memorymap.memorymap.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// The rest of the app's tests run with email verification disabled (see
// application-test.properties) so registering-then-logging-in keeps working without
// a real inbox. This class turns it back on, specifically to prove login is actually
// gated on it — the one behavior no other test covers.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.email-verification.enabled=true")
@Transactional
class EmailVerificationE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginIsRejectedUntilTheEmailIsVerified() throws Exception {
        String email = "unverified_e2e_" + System.nanoTime() + "@test.com";
        String body = """
                {"email": "%s", "password": "TestPass123!"}
                """.formatted(email);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailVerified").value(false));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Please verify your email before logging in"));
    }

    @Test
    void verifyEmailWithAnUnknownTokenIsRejectedCleanly() throws Exception {
        mockMvc.perform(get("/verify-email").param("token", "does-not-exist"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired verification link"));
    }

    @Test
    void forgotPasswordAlwaysReturnsOkRegardlessOfWhetherTheEmailExists() throws Exception {
        mockMvc.perform(post("/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"definitely-not-registered@test.com\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void resetPasswordWithAnUnknownTokenIsRejectedCleanly() throws Exception {
        mockMvc.perform(post("/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\": \"does-not-exist\", \"newPassword\": \"NewPassword123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired reset link"));
    }
}
