package org.downloader.feature.infrastructure.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.common.utils.Completer;
import org.downloader.feature.downloader.model.DownloadTask;
import org.downloader.feature.downloader.service.DownloaderService;
import org.downloader.feature.formatting.service.FormatterService;
import org.downloader.feature.formatting.model.FormattingTask;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

@Component
@AllArgsConstructor
@Slf4j
public class TaskAllocatorService {

    private final ExecutorService executorService;

    public <T> void allocateDownloadTask(
            T task,
            Consumer<T> consumer,
            Completer ack,
            Consumer<Exception> onError) {
        executorService.submit(() -> {
            try {
                consumer.accept(task);
            } catch (Exception e) {
                onError.accept(e);
            } finally {
                ack.complete();
            }
        });
    }
}
