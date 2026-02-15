package org.downloader.feature.progress.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.common.configuration.properties.FormattingEventListenerProperties;
import org.downloader.common.configuration.properties.SaveS3EventListenerProperties;
import org.downloader.common.exceptions.EventPublishException;
import org.downloader.feature.progress.model.ContentState;
import org.downloader.feature.saver.model.SaveS3Task;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;


@AllArgsConstructor
@Repository
@Slf4j
public class RedisTaskPublishRepository implements TaskPublishRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final FormattingEventListenerProperties formattingEventProperties;
    private final SaveS3EventListenerProperties saveS3EventProperties;

    @Override
    public void addFormatting(ContentState.Downloaded downloaded) {
        try {
            final String data = objectMapper.writeValueAsString(downloaded);
            redisTemplate.opsForStream().add(formattingEventProperties.stream(), Map.of("data", data));
        } catch (JsonProcessingException e) {
            log.error("error when mapping formatting task %s".formatted(downloaded));
        }
    }

    @Override
    public void addSave(SaveS3Task s3task) {
        try {
            final String data = objectMapper.writeValueAsString(s3task);
            redisTemplate.opsForStream().add(saveS3EventProperties.stream(), Map.of("data", data));
        } catch (JsonProcessingException e) {
            log.error("error when mapping completed task %s".formatted(s3task));
        }
    }
}
