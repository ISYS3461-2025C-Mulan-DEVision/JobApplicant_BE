package com.team.ja.common.exception;

public class UnsupportedFileTypeException extends FileUploadException {
    public UnsupportedFileTypeException(String message) {
        super(message, "UNSUPPORTED_FILE_TYPE");
    }
}
