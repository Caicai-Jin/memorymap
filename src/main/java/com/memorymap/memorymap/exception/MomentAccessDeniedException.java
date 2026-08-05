package com.memorymap.memorymap.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class MomentAccessDeniedException extends RuntimeException {
    public MomentAccessDeniedException(String message) {
        super(message);
    }
}
