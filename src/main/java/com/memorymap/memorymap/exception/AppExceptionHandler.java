package com.memorymap.memorymap.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// Catches any of this app's own exceptions (e.g. MomentNotFoundException,
// InvalidCredentialsException) and turns them into a clean {"message": "..."} body
// using whatever HTTP status their own @ResponseStatus annotation declares.
// Needed because @ResponseStatus alone (with no @ExceptionHandler) relies on the
// servlet container forwarding to /error to build a JSON body — that forwarding
// doesn't fully happen under MockMvc's test dispatcher, so tests saw an empty body
// even though the correct status code came through. An unannotated RuntimeException
// (a genuine bug, not one of ours) is rethrown untouched, so it still falls through
// to Spring's normal generic-500 handling instead of leaking ex.getMessage() to the client.
@RestControllerAdvice
public class AppExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleAppException(RuntimeException ex) {
        ResponseStatus responseStatus = ex.getClass().getAnnotation(ResponseStatus.class);
        if (responseStatus == null) {
            throw ex;
        }
        return ResponseEntity.status(responseStatus.value()).body(Map.of("message", ex.getMessage()));
    }
}
