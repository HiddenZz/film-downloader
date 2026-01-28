package org.downloader.feature.listener.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.common.utils.Completer;
import org.downloader.feature.downloader.model.DownloadTask;
import org.downloader.feature.formating.model.FormattingTask;
import org.downloader.feature.downloader.service.DownloaderService;
import org.downloader.feature.formating.service.FormatterService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

@Component
@AllArgsConstructor
@Slf4j
public class TaskAllocatorService {

    private final DownloaderService downloaderService;
    private final ExecutorService executorService;

    private final FormatterService formatterService;

    private final ObjectMapper objectMapper;


    void allocateDownloadTask(String type, Map<String, String> message, Completer ack, Consumer<Exception> onError) {
        executorService.submit(() -> {
            try {

                final DownloadTask task = getDataFromMessage(message, DownloadTask.class);

                if (task == null || !task.type().equals(type)) {
                    return;
                }

                downloaderService.download(task);

            } catch (Exception e) {
                onError.accept(e);
            } finally {
                ack.complete();
            }
        });
    }


    void allocateFormatingTask(Map<String, String> message, Completer ack, Consumer<Exception> onError) {
        executorService.submit(() -> {
            try {
                final FormattingTask task = getDataFromMessage(message, FormattingTask.class);

                if (task == null) {
                    return;
                }

                formatterService.format(task);

            } catch (Exception e) {
                onError.accept(e);
            } finally {
                ack.complete();
            }
        });
    }


    <T> T getDataFromMessage(Map<String, String> message, Class<T> target) throws JsonProcessingException {
        final String data = message.get("data");
        log.info(data);
        if (data == null || data.isEmpty()) {
            return null;
        }

        return objectMapper.readValue(data, target);
    }
}
