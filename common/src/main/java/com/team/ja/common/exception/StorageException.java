package com.team.ja.common.exception;

import org.springframework.http.HttpStatus;

public class StorageException extends ServiceException {
    public StorageException(String message, Throwable cause) {
        super(message, "STORAGE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
