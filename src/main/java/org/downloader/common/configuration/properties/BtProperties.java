package org.downloader.common.configuration.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("bt-config")
public record BtProperties(String tempDir) {
}
