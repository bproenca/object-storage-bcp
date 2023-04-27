package com.github.bproenca.os;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;

@Configuration
public class Config {

    @Value("${cloud.credentials.accessKey}")
    private String accessKey;

    @Value("${cloud.credentials.secretKey}")
    private String secretKey;

    @Value("${cloud.region.static}")
    private String region;

    @Value("${cloud.endpoint}")
    private String endpoint;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
            .endpoint(endpoint, 443, true)
            .credentials(accessKey, secretKey)
            .region(region)
            .build();
    }
}