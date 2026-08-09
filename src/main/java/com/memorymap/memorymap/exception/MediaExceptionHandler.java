package com.memorymap.memorymap.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

// MediaUploadNotAllowedException/MediaNotFoundException no longer need handling here —
// AppExceptionHandler now covers any of this app's own @ResponseStatus exceptions generically.
// This class only handles MaxUploadSizeExceededException, since that's thrown by Spring
// itself (not one of ours) and has no @ResponseStatus for AppExceptionHandler to read.
@RestControllerAdvice
public class MediaExceptionHandler {

    // Thrown by Spring itself when a file exceeds spring.servlet.multipart.max-file-size,
    // before the request ever reaches MediaService — previously unhandled, surfaced as a raw 500.
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of("message", "File is too large. The maximum allowed size is 50MB."));
    }
}
