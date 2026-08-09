package com.memorymap.memorymap.service;

import com.memorymap.memorymap.exception.MediaNotFoundException;
import com.memorymap.memorymap.exception.MediaUploadNotAllowedException;
import com.memorymap.memorymap.model.Media;
import com.memorymap.memorymap.model.MediaType;
import com.memorymap.memorymap.model.Moment;
import com.memorymap.memorymap.repository.MediaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class MediaService {
    private final MediaRepository mediaRepository;
    private final MomentService momentService;
    private final CloudinaryService cloudinaryService;

    public MediaService(MediaRepository mediaRepository, MomentService momentService, CloudinaryService cloudinaryService) {
        this.mediaRepository = mediaRepository;
        this.momentService = momentService;
        this.cloudinaryService = cloudinaryService;
    }

    public Media uploadMedia (Long momentId, MultipartFile file) throws IOException {
        Moment moment= momentService.getMomentById(momentId).get();
        List<Media> existingMedia= mediaRepository.findByMoment(moment);
        if(existingMedia.size()>=9){
            throw new MediaUploadNotAllowedException("Maximum 9 media items per moment");
        }
        // Checked before the Cloudinary call (not after) so an oversized file is
        // rejected instantly instead of uploading in full first and only then failing.
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new MediaUploadNotAllowedException("File can not be over 50MB");
        }
        CloudinaryUploadResult result= cloudinaryService.uploadFile(file);
        MediaType type = result.resourceType().equals("video") ? MediaType.VIDEO : MediaType.IMAGE;
        long videoCount = existingMedia.stream().filter(m -> m.getType() == MediaType.VIDEO).count();
        if(type == MediaType.VIDEO && videoCount>=3){
            cloudinaryService.deleteFile(result.publicId(), result.resourceType());
            throw new MediaUploadNotAllowedException("Maximum 3 videos per moment");
        }
        if (type == MediaType.VIDEO && result.durationSeconds() > 60) {
            cloudinaryService.deleteFile(result.publicId(), result.resourceType());
            throw new MediaUploadNotAllowedException("Video cannot be longer than 60 seconds");
        }

        Media media = new Media();
        media.setType(type);
        media.setUrl(result.url());
        media.setPublicId(result.publicId());
        media.setMoment(moment);
        return mediaRepository.save(media);

    }

    public void deleteMedia(Long momentId, Long mediaId) throws IOException {
        Moment moment = momentService.getMomentById(momentId).get();
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new MediaNotFoundException("Media not found"));
        if (!media.getMoment().getId().equals(moment.getId())) {
            throw new MediaNotFoundException("Media not found");
        }
        if (media.getPublicId() != null) {
            cloudinaryService.deleteFile(media.getPublicId(), media.getType() == MediaType.VIDEO ? "video" : "image");
        }
        mediaRepository.delete(media);
    }

    public List<Media> getMediaForMoment(Moment moment){
        return mediaRepository.findByMoment(moment);
    }

}
