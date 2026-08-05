package com.memorymap.memorymap.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MomentNotFoundException extends RuntimeException {
    public MomentNotFoundException(String message) {
        super(message);
    }
}
