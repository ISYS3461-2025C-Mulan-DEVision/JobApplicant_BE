package com.team.ja.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends ServiceException {

    public ConflictException(String message) {
        super(message, "CONFLICT", HttpStatus.CONFLICT);
    }

    public ConflictException(String resource, String field, String value) {
        super(resource + " already exists with " + field + ": " + value, "CONFLICT", HttpStatus.CONFLICT);
    }
}
