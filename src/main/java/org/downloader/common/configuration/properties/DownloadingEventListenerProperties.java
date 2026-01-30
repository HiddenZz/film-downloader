package org.downloader.common.configuration.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("downloading-listener")
public record DownloadingEventListenerProperties(String stream, String group, String consumer, String name) {
}
