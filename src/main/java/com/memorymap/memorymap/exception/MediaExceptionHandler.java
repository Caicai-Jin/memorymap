package com.memorymap.memorymap.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

// Scoped to media-upload errors only — every other exception in the app keeps
// using its own @ResponseStatus (e.g. MomentNotFoundException -> 404) via Spring's
// default handling, untouched by this class.
@RestControllerAdvice
public class MediaExceptionHandler {

    @ExceptionHandler(MediaUploadNotAllowedException.class)
    public ResponseEntity<Map<String, String>> handleMediaUploadNotAllowed(MediaUploadNotAllowedException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(MediaNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleMediaNotFound(MediaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    // Thrown by Spring itself when a file exceeds spring.servlet.multipart.max-file-size,
    // before the request ever reaches MediaService — previously unhandled, surfaced as a raw 500.
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of("message", "File is too large. The maximum allowed size is 50MB."));
    }
}
