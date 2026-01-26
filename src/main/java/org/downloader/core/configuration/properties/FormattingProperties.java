package org.downloader.core.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("formatting")
public record FormattingProperties(String stream, String consumer, String group) {
}
