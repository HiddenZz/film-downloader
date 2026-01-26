package org.downloader.core.configuration.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;

@ConfigurationProperties("bt-config")
public record BtProperties(String tempDir) {
}
