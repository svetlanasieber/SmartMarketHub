package com.smartmarkethub.smart.web.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {
    private String message;
    private String path;
    private LocalDateTime timestamp;
    private int status;
    private String error;

    public ErrorResponse(String message, String path, int status, String error) {
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
    }
}


