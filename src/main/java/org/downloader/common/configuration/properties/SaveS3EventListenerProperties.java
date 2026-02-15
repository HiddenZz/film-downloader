package org.downloader.common.configuration.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("save-s3-listener")
public record SaveS3EventListenerProperties(String stream, String consumer, String group) {
}
