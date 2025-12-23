// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\common\src\main\java\com\team\ja\common\config\S3Configuration.java

package com.team.ja.common.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

/**
 * Shared configuration for AWS S3 / SeaweedFS (S3-compatible).
 * - Reads standard aws.s3.* properties
 * - Works with AWS (no endpoint) and S3-compatible stores (custom endpoint)
 * - Path-style enabled for MinIO/SeaweedFS compatibility
 * - Exposes region and endpoint for URL building in services
 */
@Slf4j
@Getter
@Configuration
@ConditionalOnProperty(value = "aws.s3.enabled", havingValue = "true", matchIfMissing = true)
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
     * Provide a shared S3Client for all services.
     */
    @Bean
    @ConditionalOnMissingBean(S3Client.class)
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder().region(Region.of(region));

        // Credentials
        if (accessKey != null && !accessKey.isEmpty() && secretKey != null && !secretKey.isEmpty()) {
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
            builder.credentialsProvider(StaticCredentialsProvider.create(awsCredentials));
            log.info("[S3] Using static credentials (access-key provided)");
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
            log.info("[S3] Using default credentials provider (IAM/Env)");
        }

        // Custom endpoint for S3-compatible stores (SeaweedFS/MinIO)
        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
            log.info("[S3] Using custom endpoint: {}", endpoint);
        } else {
            log.info("[S3] Using AWS S3 endpoint");
        }

        // Path-style for compatibility
        builder.forcePathStyle(true);

        return builder.build();
    }
}
