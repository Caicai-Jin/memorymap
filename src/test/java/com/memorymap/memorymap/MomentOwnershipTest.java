package com.memorymap.memorymap;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Covers the Phase 2 IDOR fix: a logged-in user must never be able to read,
// edit, or delete another user's moment by guessing/incrementing an id.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MomentOwnershipTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String registerAndLogin(String email) throws Exception {
        String body = """
                {"email": "%s", "password": "TestPass123!"}
                """.formatted(email);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        // /login returns the JWT as a plain string body, not JSON.
        return mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private Long createMomentAs(String token) throws Exception {
        String response = mockMvc.perform(post("/moments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\": \"owner-only moment\", \"mood\": [\"happy\"]}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void ownerCanAccessTheirOwnMoment() throws Exception {
        String ownerToken = registerAndLogin("owner_" + System.nanoTime() + "@test.com");
        Long momentId = createMomentAs(ownerToken);

        mockMvc.perform(get("/moments/" + momentId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());
    }

    @Test
    void nonOwnerCannotReadAnotherUsersMoment() throws Exception {
        String ownerToken = registerAndLogin("owner_" + System.nanoTime() + "@test.com");
        String intruderToken = registerAndLogin("intruder_" + System.nanoTime() + "@test.com");
        Long momentId = createMomentAs(ownerToken);

        mockMvc.perform(get("/moments/" + momentId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void nonOwnerCannotUpdateAnotherUsersMoment() throws Exception {
        String ownerToken = registerAndLogin("owner_" + System.nanoTime() + "@test.com");
        String intruderToken = registerAndLogin("intruder_" + System.nanoTime() + "@test.com");
        Long momentId = createMomentAs(ownerToken);

        mockMvc.perform(put("/moments/" + momentId)
                        .header("Authorization", "Bearer " + intruderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\": \"hacked\", \"mood\": []}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void nonOwnerCannotDeleteAnotherUsersMoment() throws Exception {
        String ownerToken = registerAndLogin("owner_" + System.nanoTime() + "@test.com");
        String intruderToken = registerAndLogin("intruder_" + System.nanoTime() + "@test.com");
        Long momentId = createMomentAs(ownerToken);

        mockMvc.perform(delete("/moments/" + momentId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isForbidden());
    }
}
