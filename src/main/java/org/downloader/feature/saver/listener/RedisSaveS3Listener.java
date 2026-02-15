package org.downloader.feature.saver.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.common.configuration.properties.FormattingEventListenerProperties;
import org.downloader.common.configuration.properties.SaveS3EventListenerProperties;
import org.downloader.feature.formatting.model.FormattingTask;
import org.downloader.feature.formatting.service.FormatterService;
import org.downloader.feature.infrastructure.redis.TaskAllocatorService;
import org.downloader.feature.saver.model.SaveS3Task;
import org.downloader.feature.saver.service.SaveS3Service;
import org.downloader.feature.saver.service.SaveS3ServiceImpl;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class RedisSaveS3Listener implements SaveS3ListenerService {

    private final StringRedisTemplate redis;
    private final SaveS3EventListenerProperties properties;
    private final TaskAllocatorService allocatorService;
    private final SaveS3Service saveS3Service;
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
                    objectMapper.readValue(data, SaveS3Task.class),
                    saveS3Service,
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
