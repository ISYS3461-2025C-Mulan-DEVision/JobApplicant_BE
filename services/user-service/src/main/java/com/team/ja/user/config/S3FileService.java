package com.team.ja.user.config;

import com.team.ja.common.config.S3Configuration;
import com.team.ja.common.exception.StorageException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Service for handling file uploads and downloads to/from AWS S3 or a
 * compatible service like SeaweedFS.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileService {

    private final S3Client s3Client;
    private final S3Configuration s3Configuration;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        return uploadFile(file.getBytes(), file.getOriginalFilename(), file.getContentType(), folder);
    }

    public String uploadFile(InputStream inputStream, String originalFileName, String contentType, String folder)
            throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        return uploadFile(bytes, originalFileName, contentType, folder);
    }

    public String uploadFile(byte[] bytes, String originalFileName, String contentType, String folder) {
        String key = folder + "/" + generateUniqueFileName(originalFileName);
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
            log.info("File uploaded successfully to S3 (public): {}", key);
            return buildFileUrl(key);
        } catch (Exception e) {
            log.error("Error uploading file to S3: {}", e.getMessage(), e);
            throw new StorageException("Failed to upload file to S3", e);
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        try {
            String key = extractKeyFromUrl(fileUrl);
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName).key(key).build();
            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully from S3: {}", key);
        } catch (Exception e) {
            log.error("Error deleting file from S3: {}", e.getMessage(), e);
            throw new StorageException("Failed to delete file from S3", e);
        }
    }

    private String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        return UUID.randomUUID() + "_" + System.currentTimeMillis() + extension;
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }

    private String buildFileUrl(String key) {

        String endpoint = s3Configuration.getPublicEndpoint();
        if(endpoint == null || endpoint.isEmpty()) {
            endpoint = s3Configuration.getEndpoint();
        }
        if (endpoint != null && !endpoint.isEmpty()) {
            return String.format("%s/%s/%s", endpoint, bucketName, key);
        }
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, s3Configuration.getRegion(), key);
    }

    private String extractKeyFromUrl(String fileUrl) {
        try {
            java.net.URL url = new java.net.URL(fileUrl);
            // Path is /bucketName/key, so we skip the first slash
            return url.getPath().substring(1 + bucketName.length() + 1);
        } catch (Exception e) {
            log.warn("Could not parse URL to extract key: {}", fileUrl, e);
            // Fallback for simple path structures
            String[] parts = fileUrl.split("/", 5);
            if (parts.length >= 5) {
                return parts[4];
            }
            return "";
        }
    }
}