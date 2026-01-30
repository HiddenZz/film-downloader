package org.downloader.feature.formatting.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.common.configuration.properties.FormattingEventListenerProperties;
import org.downloader.feature.formatting.model.FormattingTask;
import org.downloader.feature.formatting.service.FormatterService;
import org.downloader.feature.infrastructure.redis.TaskAllocatorService;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@AllArgsConstructor
@Slf4j
public class RedisFormattingListener implements FormattingListenerService {

    private final StringRedisTemplate redis;
    private final FormattingEventListenerProperties properties;
    private final TaskAllocatorService allocatorService;
    private final FormatterService formatterService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        try {
            final String data = message.getValue().get("data");

            if (data == null || data.isEmpty()) {
                ack(message);
                return;
            }

            allocatorService.allocateDownloadTask(
                    objectMapper.readValue(data, FormattingTask.class),
                    formatterService,
                    () -> ack(message),
                    e -> log.error("Error processing message {} for stream {} group {}",
                                   message.getId(), properties.stream(), properties.group(), e)
            );

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void ack(MapRecord<String, String, String> message) {
        redis.opsForStream().acknowledge(properties.stream(), properties.group(), message.getId());
    }
}
