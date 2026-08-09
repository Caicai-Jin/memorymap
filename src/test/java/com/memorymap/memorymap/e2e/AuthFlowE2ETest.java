package com.memorymap.memorymap.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Full register -> login -> use-the-token round trip through the real HTTP layer.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthFlowE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerThenLoginThenAccessAProtectedEndpoint() throws Exception {
        String email = "auth_flow_" + System.nanoTime() + "@test.com";
        String body = """
                {"email": "%s", "password": "TestPass123!"}
                """.formatted(email);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        String token = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get("/moments").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointRejectsRequestsWithNoToken() throws Exception {
        mockMvc.perform(get("/moments"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registeringWithATooShortPasswordIsRejected() throws Exception {
        String body = """
                {"email": "%s", "password": "short"}
                """.formatted("short_pw_" + System.nanoTime() + "@test.com");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registeringWithAMalformedEmailIsRejected() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"not-an-email\", \"password\": \"TestPass123!\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void duplicateEmailRegistrationIsRejectedCleanly() throws Exception {
        String email = "auth_flow_dup_" + System.nanoTime() + "@test.com";
        String body = """
                {"email": "%s", "password": "TestPass123!"}
                """.formatted(email);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already in use"));
    }

    @Test
    void loginWithWrongPasswordIsRejectedCleanly() throws Exception {
        String email = "auth_flow_wrongpw_" + System.nanoTime() + "@test.com";
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"" + email + "\", \"password\": \"TestPass123!\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"" + email + "\", \"password\": \"WrongPassword1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}
