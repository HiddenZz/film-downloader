package org.downloader.feature.listener.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.core.configuration.properties.DownloaderProperties;
import org.downloader.core.configuration.properties.FormattingProperties;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class StreamSetup {

    final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;

    final DownloadListenerService downloadListenerService;
    final DownloaderProperties downloaderProperties;

    final FormattingProperties formattingProperties;
    final FormattingListenerService formattingListenerService;
    
    @PostConstruct
    void init() {
        startDownloadListening();
        startFormattingListening();

        listenerContainer.start();
    }

    private void startDownloadListening() {
        listenerContainer.receive(Consumer.from(downloaderProperties.group(), downloaderProperties.consumer()), StreamOffset.create(downloaderProperties.stream(), ReadOffset.lastConsumed()), downloadListenerService);

        startingLog("downloading", downloaderProperties.stream());
    }

    private void startFormattingListening() {
        listenerContainer.receive(Consumer.from(formattingProperties.group(), formattingProperties.consumer()), StreamOffset.create(formattingProperties.stream(), ReadOffset.lastConsumed()), formattingListenerService);

        startingLog("formatting", formattingProperties.stream());
    }

    private void startingLog(String name, String streamName) {
        log.info("Started {} listening to stream: {}", name, streamName);
    }

    @PreDestroy
    void dispose() {
        listenerContainer.stop();
    }
}
