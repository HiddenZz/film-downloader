package org.downloader.feature.listener.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.core.configuration.properties.DownloaderProperties;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class RedisDownloadListener implements DownloadListenerService {

    private final StringRedisTemplate redis;
    private final DownloaderProperties properties;
    private final TaskAllocatorService taskAllocatorService;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        taskAllocatorService.allocateDownloadTask(properties.name(), message.getValue(), () -> ack(message), (e) -> log.error("Error when during new message {} for stream {} group {}", message, properties.stream(), properties.group(), e));
    }

    private void ack(MapRecord<String, String, String> message) {
        redis.opsForStream().acknowledge(properties.stream(), properties.group(), message.getId());
    }
}
