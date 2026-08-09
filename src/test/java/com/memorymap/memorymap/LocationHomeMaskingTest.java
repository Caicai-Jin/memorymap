package com.memorymap.memorymap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Covers the Phase 3 privacy requirement: a HOME location must never expose its
// real address/coordinates in a moment response, only the literal name "Home".
// A PUBLIC location, by contrast, should show its real data unchanged.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LocationHomeMaskingTest {

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

        return mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private Long createLocationAs(String token, String type) throws Exception {
        String body = """
                {"name": "My House", "address": "123 Secret St", "latitude": 43.4, "longitude": -80.5, "type": "%s"}
                """.formatted(type);

        String response = mockMvc.perform(post("/locations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void homeLocationIsMaskedOnAMoment() throws Exception {
        String token = registerAndLogin("home_mask_" + System.nanoTime() + "@test.com");
        Long homeLocationId = createLocationAs(token, "HOME");

        String momentBody = """
                {"content": "at home", "mood": [], "location": {"id": %d}}
                """.formatted(homeLocationId);

        String response = mockMvc.perform(post("/moments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(momentBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode location = objectMapper.readTree(response).get("location");
        assertEquals("Home", location.get("name").asText());
        assertTrue(location.get("address").isNull());
        assertTrue(location.get("latitude").isNull());
        assertTrue(location.get("longitude").isNull());
    }

    @Test
    void publicLocationIsNotMaskedOnAMoment() throws Exception {
        String token = registerAndLogin("public_loc_" + System.nanoTime() + "@test.com");
        Long publicLocationId = createLocationAs(token, "PUBLIC");

        String momentBody = """
                {"content": "at the cafe", "mood": [], "location": {"id": %d}}
                """.formatted(publicLocationId);

        String response = mockMvc.perform(post("/moments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(momentBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode location = objectMapper.readTree(response).get("location");
        assertEquals("My House", location.get("name").asText());
        assertEquals("123 Secret St", location.get("address").asText());
    }
}
