package org.downloader.feature.downloader.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.common.configuration.properties.DownloadingEventListenerProperties;
import org.downloader.feature.downloader.model.DownloadTask;
import org.downloader.feature.downloader.service.DownloaderService;
import org.downloader.feature.downloader.service.TaskDeserializer;
import org.downloader.feature.infrastructure.redis.TaskAllocatorService;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;

@AllArgsConstructor
@Slf4j
public abstract class RedisDownloadListener<T extends DownloadTask> implements DownloadListenerService {

    private final StringRedisTemplate redis;
    private final DownloadingEventListenerProperties properties;
    private final TaskAllocatorService taskAllocatorService;
    private final TaskDeserializer<T> taskDeserializer;
    private final DownloaderService<T> downloaderService;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        try {
            T task = taskDeserializer.deserialize(message.getValue());
            if (task == null) {
                log.warn("Empty or invalid message: {}", message.getId());
                ack(message);
                return;
            }

            taskAllocatorService.allocateDownloadTask(
                    task,
                    downloaderService,
                    () -> ack(message),
                    e -> log.error("Error processing message {} for stream {} group {}",
                                   message.getId(), properties.stream(), properties.group(), e)
            );
            
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize message: {}", message.getId(), e);
            ack(message);
        }
    }

    private void ack(MapRecord<String, String, String> message) {
        redis.opsForStream().acknowledge(properties.stream(), properties.group(), message.getId());
    }
}
