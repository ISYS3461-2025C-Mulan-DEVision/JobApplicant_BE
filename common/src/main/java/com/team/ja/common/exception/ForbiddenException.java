package com.team.ja.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ServiceException {

    public ForbiddenException(String message) {
        super(message, "FORBIDDEN", HttpStatus.FORBIDDEN);
    }

    public ForbiddenException() {
        super("Access denied", "FORBIDDEN", HttpStatus.FORBIDDEN);
    }
}
