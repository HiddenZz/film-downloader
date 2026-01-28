package org.downloader.feature.progress.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.common.configuration.properties.DownloaderProperties;
import org.downloader.common.configuration.properties.FormattingProperties;
import org.downloader.common.exceptions.EventPublishException;
import org.downloader.feature.progress.model.ContentState;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentEventPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final DownloaderProperties downloaderProperties;
    private final FormattingProperties formattingProperties;

    public void sendToFormatting(ContentState.DownloadSuccess state) {
        publish(formattingProperties.stream(), state);
    }

    public void sendCompleted(ContentState.Success state) {
        publish(downloaderProperties.stream(), state);
    }

    private void publish(String stream, Object payload) {
        try {
            String data = objectMapper.writeValueAsString(payload);
            redisTemplate.opsForStream().add(stream, Map.of("data", data));
            log.debug("Published to {}: {}", stream, data);
        } catch (Exception e) {
            log.error("Failed to publish to {}", stream, e);
            throw new EventPublishException("Failed to publish event", e);
        }
    }
}