package org.downloader.common.configuration.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("downloader")
public record DownloaderProperties(String stream, String group, String consumer, String name) {
}
