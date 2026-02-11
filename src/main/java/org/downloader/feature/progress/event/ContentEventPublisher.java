package org.downloader.feature.progress.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.common.configuration.properties.DownloadingEventListenerProperties;
import org.downloader.common.configuration.properties.FormattingEventListenerProperties;
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
    private final DownloadingEventListenerProperties downloadingEventListenerProperties;
    private final FormattingEventListenerProperties formattingEventListenerProperties;

    public void sendToFormatting(ContentState.Downloaded state) {
        publish(formattingEventListenerProperties.stream(), state);
    }

    public void sendCompleted(ContentState.Completed state) {
        
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
