package com.memorymap.memorymap.dto;

import com.memorymap.memorymap.model.MediaType;

public record MediaResponse(Long id, MediaType type, String url){
}
