package com.team.ja.common.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ServiceException {

    public NotFoundException(String message) {
        super(message, "NOT_FOUND", HttpStatus.NOT_FOUND);
    }

    public NotFoundException(String resource, Long id) {
        super(resource + " not found with id: " + id, "NOT_FOUND", HttpStatus.NOT_FOUND);
    }

    public NotFoundException(String resource, String field, String value) {
        super(resource + " not found with " + field + ": " + value, "NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}

