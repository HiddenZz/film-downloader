package org.downloader.common.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("formatting-listener")
public record FormattingEventListenerProperties(String stream, String consumer, String group) {
}
