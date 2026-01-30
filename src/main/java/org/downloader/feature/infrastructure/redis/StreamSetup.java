package org.downloader.feature.infrastructure.redis;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.common.configuration.properties.DownloadingEventListenerProperties;
import org.downloader.common.configuration.properties.FormattingEventListenerProperties;
import org.downloader.feature.downloader.listener.DownloadListenerService;
import org.downloader.feature.formatting.listener.FormattingListenerService;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class StreamSetup {

    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private final StringRedisTemplate redisTemplate;

    private final DownloadListenerService downloadListenerService;
    private final DownloadingEventListenerProperties downloadingEventListenerProperties;

    private final FormattingEventListenerProperties formattingEventListenerProperties;
    private final FormattingListenerService formattingListenerService;

    @PostConstruct
    void init() {
        createStreamAndGroup(downloadingEventListenerProperties.stream(), downloadingEventListenerProperties.group());
        createStreamAndGroup(formattingEventListenerProperties.stream(), formattingEventListenerProperties.group());

        startDownloadListening();
        startFormattingListening();
        listenerContainer.start();
    }

    private void createStreamAndGroup(String stream, String group) {
        try {

            assert redisTemplate.getConnectionFactory() != null;
            final RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            connection.streamCommands().xGroupCreate(
                    stream.getBytes(),
                    group,
                    ReadOffset.from("0"),
                    true
            );

            log.info("Created stream '{}' with group '{}'", stream, group);
        } catch (RedisSystemException e) {
            if (e.getCause() != null && e.getCause().getMessage() != null
                    && e.getCause().getMessage().contains("BUSYGROUP")) {
                log.debug("Consumer group '{}' already exists for stream '{}'", group, stream);
                return;
            }
            throw e;
        }
    }

    private void startDownloadListening() {
        listenerContainer.receive(
                Consumer.from(
                        downloadingEventListenerProperties.group(),
                        downloadingEventListenerProperties.consumer()
                ),
                StreamOffset.create(
                        downloadingEventListenerProperties.stream(),
                        ReadOffset.lastConsumed()
                ),
                downloadListenerService
        );
        log.info("Started downloading listener on stream: {}", downloadingEventListenerProperties.stream());
    }

    private void startFormattingListening() {
        listenerContainer.receive(
                Consumer.from(
                        formattingEventListenerProperties.group(),
                        formattingEventListenerProperties.consumer()
                ),
                StreamOffset.create(
                        formattingEventListenerProperties.stream(),
                        ReadOffset.lastConsumed()
                ),
                formattingListenerService
        );

        log.info("Started formatting listener on stream: {}", formattingEventListenerProperties.stream());
    }

    @PreDestroy
    void dispose() {
        listenerContainer.stop();
    }
}
