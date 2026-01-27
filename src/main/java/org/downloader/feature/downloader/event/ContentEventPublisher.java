package org.downloader.feature.downloader.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.common.exceptions.EventPublishException;
import org.downloader.feature.downloader.model.ContentState;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentEventPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void sendToFormatting(ContentState.DownloadSuccess state) {
        publish("formatting:stream", state);
    }

    public void sendCompleted(ContentState.Success state) {
        publish("content:completed", state);
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