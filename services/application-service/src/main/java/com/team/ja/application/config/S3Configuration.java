// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\services\application-service\src\main\java\com\team\ja\application\config\S3Configuration.java

package com.team.ja.application.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

/**
 * Configuration for AWS S3 / MinIO S3-compatible storage.
 * Supports both AWS S3 and MinIO based on application properties.
 */
@Slf4j
@Configuration
public class S3Configuration {

    @Value("${aws.s3.access-key:}")
    private String accessKey;

    @Value("${aws.s3.secret-key:}")
    private String secretKey;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Value("${aws.s3.endpoint:}")
    private String endpoint;

    /**
     * Create S3Client bean configured for AWS S3 or MinIO.
     */
    @Bean
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder();

        // Set region
        builder.region(Region.of(region));

        // Set credentials if provided
        if (accessKey != null && !accessKey.isEmpty() && secretKey != null && !secretKey.isEmpty()) {
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
            builder.credentialsProvider(StaticCredentialsProvider.create(awsCredentials));
            log.info("S3 client configured with provided credentials");
        } else {
            log.info("S3 client configured with default credentials provider (IAM/Environment)");
        }

        // Set custom endpoint for MinIO
        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
            log.info("S3 client configured with custom endpoint: {}", endpoint);
        } else {
            log.info("S3 client configured to use AWS S3 endpoint");
        }

        // Force path-style access for MinIO compatibility
        builder.forcePathStyle(true);

        return builder.build();
    }
}
