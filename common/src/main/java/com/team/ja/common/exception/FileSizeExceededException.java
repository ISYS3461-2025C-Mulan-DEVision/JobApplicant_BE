package com.team.ja.common.exception;

public class FileSizeExceededException extends FileUploadException {
    public FileSizeExceededException(String message) {
        super(message, "FILE_SIZE_EXCEEDED");
    }
}
