package com.memorymap.memorymap.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret
    ) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    public CloudinaryUploadResult uploadFile(MultipartFile file) throws IOException {
        // "auto" lets Cloudinary detect image vs video from the file itself,
        // instead of trusting a type the client sends alongside it.
        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("resource_type", "auto")
        );

        String url = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");
        String resourceType = (String) uploadResult.get("resource_type");
        long bytes = ((Number) uploadResult.get("bytes")).longValue();
        Double durationSeconds = uploadResult.get("duration") != null
                ? ((Number) uploadResult.get("duration")).doubleValue()
                : null;

        return new CloudinaryUploadResult(url, publicId, resourceType, bytes, durationSeconds);
    }

    // Used to clean up a file that got rejected by our own limits (e.g. video too long)
    // after it was already uploaded — otherwise it sits in Cloudinary storage forever, unused.
    public void deleteFile(String publicId, String resourceType) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
    }
}