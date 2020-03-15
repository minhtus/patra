package com.prc391.patra.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandlerController {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity notFoundHandler() {
        return ResponseEntity.notFound().build();
    }
}
