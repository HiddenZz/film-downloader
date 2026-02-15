package org.downloader.common.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage.s3")
public record S3StorageProperties(String topPrefix, String hlsPlaylistContentType) {
}
