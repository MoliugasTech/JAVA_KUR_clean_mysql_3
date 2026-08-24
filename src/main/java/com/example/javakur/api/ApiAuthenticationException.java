package com.example.javakur.api;

import org.springframework.http.HttpStatus;

public class ApiAuthenticationException extends RuntimeException {
    private final HttpStatus status;

    public ApiAuthenticationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
