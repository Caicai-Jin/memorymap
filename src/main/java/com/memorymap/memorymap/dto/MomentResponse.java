package com.memorymap.memorymap.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MomentResponse(Long id, String content, List<String> mood, LocalDateTime createdAt, LocalDateTime updatedAt, LocationResponse location, List<MediaResponse> mediaResponses) {
}

//What a DTO is: "Data Transfer Object" —
// a plain object whose only job is to define exactly
// what shape of data crosses the wire (server → client).
// It's not connected to the database at all;
// it's just a data container you build fresh each time.