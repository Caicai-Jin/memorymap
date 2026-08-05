package com.memorymap.memorymap.dto;

import com.memorymap.memorymap.model.LocationType;

public record LocationResponse(Long id, String name, String address, Double latitude, Double longitude, LocationType type) {
}