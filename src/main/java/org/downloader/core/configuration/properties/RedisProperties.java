package org.downloader.core.configuration.properties;



import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;

@ConfigurationProperties("storage.redis")
@ConfigurationPropertiesBinding
public record RedisProperties(String url, int port) {

}
