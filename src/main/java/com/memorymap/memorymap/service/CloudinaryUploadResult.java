package com.memorymap.memorymap.service;

// Cloudinary tells us the resource type ("image"/"video"), file size, and
// (for video) duration — safer to trust these than whatever the client claims.
// durationSeconds is null for images, since Cloudinary only reports it for video/audio.
public record CloudinaryUploadResult(String url, String publicId, String resourceType, long bytes, Double durationSeconds) {
}