// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\services\application-service\src\main\java\com\team\ja\application\config\S3FileService.java

package com.team.ja.user.config;

import com.team.ja.common.config.S3Configuration;
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
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Service for handling file uploads and downloads to/from AWS S3 or MinIO.
 * Implements standard S3 operations for application files.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileService {

    private final S3Client s3Client;
    private final S3Configuration s3Configuration;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    /**
     * Upload a file to S3 and return the file URL.
     *
     * @param file The file to upload
     * @param folder The folder path in S3 (e.g., "applications/resumes")
     * @return The URL of the uploaded file
     */
    public String uploadFile(MultipartFile file, String folder)
        throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        return uploadFile(
            file.getBytes(),
            file.getOriginalFilename(),
            file.getContentType(),
            folder
        );
    }

    /**
     * Upload a file from an InputStream to S3.
     *
     * @param inputStream The InputStream of the file
     * @param originalFileName The original name of the file
     * @param contentType The content type of the file
     * @param folder The folder path in S3
     * @return The URL of the uploaded file
     */
    public String uploadFile(
        InputStream inputStream,
        String originalFileName,
        String contentType,
        String folder
    ) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        return uploadFile(bytes, originalFileName, contentType, folder);
    }

    public String uploadFile(
        byte[] bytes,
        String originalFileName,
        String contentType,
        String folder
    ) throws IOException {
        // Generate unique filename
        String fileName = generateUniqueFileName(originalFileName);
        String key = folder + "/" + fileName;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));

            log.info("File uploaded successfully: {}", key);
            return buildFileUrl(key);
        } catch (Exception e) {
            log.error("Error uploading file to S3: {}", e.getMessage(), e);
            // SeaweedFS may reject signed requests; fallback to unsigned HTTP PUT when using a custom endpoint
            String endpoint = s3Configuration.getEndpoint();
            boolean canTryUnsigned = endpoint != null && !endpoint.isEmpty();
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (
                canTryUnsigned &&
                (msg.contains("Signed request requires") ||
                    msg.contains("Signature") ||
                    msg.contains("400"))
            ) {
                try {
                    putUnsignedObject(
                        endpoint,
                        bucketName,
                        key,
                        bytes,
                        contentType
                    );
                    log.info("File uploaded via unsigned HTTP PUT: {}", key);
                    return buildFileUrl(key);
                } catch (Exception ex) {
                    log.error(
                        "Unsigned HTTP PUT fallback failed: {}",
                        ex.getMessage(),
                        ex
                    );
                }
            }
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    /**
     * Delete a file from S3.
     *
     * @param fileUrl The URL of the file to delete
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // Extract key from URL
            String key = extractKeyFromUrl(fileUrl);

            DeleteObjectRequest deleteObjectRequest =
                DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

            log.info("File deleted successfully: {}", key);
        } catch (Exception e) {
            log.error("Error deleting file from S3: {}", e.getMessage(), e);
            // Don't throw exception on delete failure, just log it
        }
    }

    /**
     * Generate a unique filename to avoid conflicts.
     */
    private String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        return UUID.randomUUID() + "_" + System.currentTimeMillis() + extension;
    }

    /**
     * Extract file extension from filename.
     */
    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }

    /**
     * Build the full URL for a file in S3/MinIO.
     */
    private String buildFileUrl(String key) {
        String endpoint = s3Configuration.getEndpoint();
        String region = s3Configuration.getRegion();

        if (endpoint != null && !endpoint.isEmpty()) {
            // MinIO or custom S3-compatible endpoint
            return String.format("%s/%s/%s", endpoint, bucketName, key);
        } else {
            // AWS S3
            return String.format(
                "https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                region,
                key
            );
        }
    }

    // Fallback: unsigned HTTP PUT for SeaweedFS when signing is not configured
    private void putUnsignedObject(
        String endpoint,
        String bucket,
        String key,
        byte[] bytes,
        String contentType
    ) throws IOException {
        String sep = endpoint.endsWith("/") ? "" : "/";
        String urlStr = endpoint + sep + bucket + "/" + key;
        java.net.URL url = new java.net.URL(urlStr);
        java.net.HttpURLConnection conn =
            (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setDoOutput(true);
        if (contentType != null && !contentType.isEmpty()) {
            conn.setRequestProperty("Content-Type", contentType);
        }
        conn.setFixedLengthStreamingMode(bytes.length);
        try (java.io.OutputStream os = conn.getOutputStream()) {
            os.write(bytes);
        }
        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IOException("Unsigned PUT failed with HTTP " + code);
        }
    }

    /**
     * Extract S3 key from file URL.
     */
    private String extractKeyFromUrl(String fileUrl) {
        // Remove bucket and domain info to get the key
        String[] parts = fileUrl.split("/", 4);
        if (parts.length >= 4) {
            return parts[3];
        }
        return fileUrl;
    }
}
