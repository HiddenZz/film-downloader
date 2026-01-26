package org.downloader.core.configuration.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;

@ConfigurationProperties("downloader")
public record DownloaderProperties(String stream, String group, String consumer, String name) {
}
