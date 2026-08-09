package com.memorymap.memorymap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.memorymap.memorymap.model.Media;
import com.memorymap.memorymap.model.Moment;
import com.memorymap.memorymap.repository.MediaRepository;
import com.memorymap.memorymap.repository.MomentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.memorymap.memorymap.model.MediaType.IMAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Covers the Phase 4 media limit: a moment can hold at most 9 media items.
// The 9 existing items are seeded directly via the repository (skipping real
// Cloudinary uploads) since MediaService checks the count *before* ever calling
// Cloudinary, so this test never touches the real Cloudinary account.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MediaLimitTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MomentRepository momentRepository;
    @Autowired
    private MediaRepository mediaRepository;

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

    private Long createMomentAs(String token) throws Exception {
        String response = mockMvc.perform(post("/moments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\": \"media limit test\", \"mood\": []}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void tenthMediaItemIsRejected() throws Exception {
        String token = registerAndLogin("media_limit_" + System.nanoTime() + "@test.com");
        Long momentId = createMomentAs(token);
        Moment moment = momentRepository.findById(momentId).orElseThrow();

        for (int i = 0; i < 9; i++) {
            Media media = new Media();
            media.setType(IMAGE);
            media.setUrl("https://example.com/fake-" + i + ".jpg");
            media.setPublicId("fake-" + i);
            media.setMoment(moment);
            mediaRepository.save(media);
        }

        MockMultipartFile file = new MockMultipartFile(
                "file", "tenth.jpg", "image/jpeg", "fake image bytes".getBytes());

        String response = mockMvc.perform(multipart("/moments/" + momentId + "/media")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        assertEquals("Maximum 9 media items per moment", json.get("message").asText());
    }
}
