package com.memorymap.memorymap.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateHomeLocationException extends RuntimeException {
    public DuplicateHomeLocationException(String message) {
        super(message);
    }
}