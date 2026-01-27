package org.downloader.common.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("storage.minio.credentials")
public class MinIoClientProperties {
    private String endpoint;
    private String name;
    private String password;
}
