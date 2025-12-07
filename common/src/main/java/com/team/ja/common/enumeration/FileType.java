package com.team.ja.common.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * File types supported by the system.
 * Used for file type tracking.
 */
@Getter
@RequiredArgsConstructor
public enum FileType {
    IMAGE("Image", "Image type"),
    VIDEO("Video", "Video type"),
    DOC("Document", "Document type");

    private final String displayName;
    private final String description;
}
