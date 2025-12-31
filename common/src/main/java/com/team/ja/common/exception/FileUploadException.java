package com.team.ja.common.exception;

import org.springframework.http.HttpStatus;

public class FileUploadException extends ServiceException {
    public FileUploadException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST);
    }
}
