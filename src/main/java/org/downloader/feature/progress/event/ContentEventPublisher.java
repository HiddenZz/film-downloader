package org.downloader.feature.progress.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.feature.progress.model.ContentState;
import org.downloader.feature.progress.repository.TaskPublishRepository;
import org.downloader.feature.saver.model.SaveS3Task;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentEventPublisher {

    private final TaskPublishRepository publishRepository;

    public void sendToFormatting(ContentState.Downloaded state) {
        try {
            publishRepository.addFormatting(state);
        } catch (Exception e) {
            log.error("Failed to publish to formatting task %s".formatted(state), e);
        }
    }

    public void sendSaveToS3(ContentState.Formatted state) {
        try {
            publishRepository.addSave(SaveS3Task.builder()
                                              .contentUuid(state.getContentUuid())
                                              .tmdbId(state.getTmdbId())
                                              .masterPlaylistPath(state.getMasterPlaylistPath())
                                              .build());
        } catch (Exception e) {
            log.error("Failed to publish to save task %s".formatted(state), e);
        }
    }
}
