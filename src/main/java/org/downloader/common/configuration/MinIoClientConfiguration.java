package org.downloader.common.configuration;

import io.minio.MinioClient;
import org.downloader.common.configuration.properties.MinIoClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(MinIoClientProperties.class)
@Configuration
public class MinIoClientConfiguration {

    @Bean
    MinioClient minioClient(MinIoClientProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getName(), properties.getPassword())
                .build();
    }
}
