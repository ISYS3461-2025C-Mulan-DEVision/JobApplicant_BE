// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\services\application-service\src\main\java\com\team\ja\application\config\S3FileService.java

package com.team.ja.application.config;

import com.team.ja.common.config.S3Configuration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

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
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Generate unique filename
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        String key = folder + "/" + fileName;

        try {
            // Upload to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("File uploaded successfully: {}", key);

            // Return the file URL
            return buildFileUrl(key);
        } catch (Exception e) {
            log.error("Error uploading file to S3: {}", e.getMessage(), e);
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

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
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
     * Download a file from S3 and return its content as byte array.
     *
     * @param fileUrl The URL of the file to download
     * @return Byte array of the file content
     */
    public byte[] downloadFile(String fileUrl) throws IOException {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new IllegalArgumentException("File URL cannot be empty");
        }

        try {
            // Extract key from URL
            String key = extractKeyFromUrl(fileUrl);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            InputStream inputStream = s3Client.getObject(getObjectRequest);
            byte[] fileContent = inputStream.readAllBytes();
            inputStream.close();

            log.info("File downloaded successfully: {}", key);
            return fileContent;
        } catch (Exception e) {
            log.error("Error downloading file from S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download file from S3", e);
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
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
        }
    }

    /**
     * Extract S3 key from file URL.
     * URL format: http://seaweedfs:8333/bucket-name/folder/file.pdf
     * Returns: folder/file.pdf (without bucket name)
     */
    private String extractKeyFromUrl(String fileUrl) {
        try {
            // Split URL by "/" to extract parts
            String[] parts = fileUrl.split("/");
            
            // parts[0] = "http:"
            // parts[1] = ""
            // parts[2] = "seaweedfs:8333"
            // parts[3] = bucket name
            // parts[4+] = actual key path
            
            if (parts.length > 4) {
                // Reconstruct the key path starting from index 4 (skip bucket name)
                StringBuilder keyBuilder = new StringBuilder();
                for (int i = 4; i < parts.length; i++) {
                    if (i > 4) keyBuilder.append("/");
                    keyBuilder.append(parts[i]);
                }
                return keyBuilder.toString();
            }
            
            // Fallback if URL format is unexpected
            log.warn("Unexpected URL format: {}", fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("Error extracting key from URL: {}", fileUrl, e);
            return fileUrl;
        }
    }
}
