package com.example.personalmemory.exception;

import java.time.Instant;

public class ApiError {
    private final Instant timestamp = Instant.now();
    private int status;
    private String message;
    private String debug;

    public ApiError(int status, String message, String debug) {
        this.status = status;
        this.message = message;
        this.debug = debug;
    }

    // getters
    public Instant getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public String getDebug() { return debug; }
}
