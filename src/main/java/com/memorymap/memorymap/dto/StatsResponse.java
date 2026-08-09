package com.memorymap.memorymap.dto;

import java.util.Map;

public record StatsResponse(int year, String dominantMood, Map<String, Long> moodBreakdown, LocationResponse happiestLocation, LocationResponse
    saddestLocation, LocationResponse mostEmotionallyDiverseLocation) {
    }

