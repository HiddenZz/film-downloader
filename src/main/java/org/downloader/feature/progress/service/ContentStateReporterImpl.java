package org.downloader.feature.progress.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.feature.progress.event.ContentEventPublisher;
import org.downloader.feature.progress.model.ContentDto;
import org.downloader.feature.progress.model.ContentState;
import org.downloader.feature.progress.repository.ContentRepository;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
@Slf4j
public class ContentStateReporterImpl implements ContentStateReporter {

    private final ContentRepository contentRepository;
    private final ContentEventPublisher eventPublisher;

    @Override
    public void report(ContentState state) {
        switch (state) {
            case ContentState.Downloading s -> handleDownloading(s);
            case ContentState.Downloaded s -> handleDownloaded(s);
            case ContentState.DownloadFailed s -> handleDownloadFailed(s);
            case ContentState.Formatting s -> handleFormatting(s);
            case ContentState.Formatted s -> handleFormatted(s);
            case ContentState.FormatFailed s -> handleFormatFailed(s);
            case ContentState.Completed s -> handleCompleted(s);
        }
    }


    private void handleDownloading(ContentState.Downloading state) {
        final ContentDto dto = toDto(state);
        if (contentRepository.isExists(state.getContentUuid())) {
            contentRepository.updateState(dto);
            return;
        }

        contentRepository.create(dto);
    }

    private void handleDownloaded(ContentState.Downloaded state) {
        contentRepository.updateState(toDto(state));
        eventPublisher.sendToFormatting(state);
    }

    private void handleDownloadFailed(ContentState.DownloadFailed state) {
        contentRepository.updateState(ContentDto.builder()
                                              .tmdbId(state.getTmdbId())
                                              .contentUuid(state.getContentUuid())
                                              .state(state.name())
                                              .errorCause(state.getCause())
                                              .build());
    }

    private void handleFormatting(ContentState.Formatting state) {
        contentRepository.updateState(toDto(state));
    }

    private void handleFormatted(ContentState.Formatted state) {
        contentRepository.updateState(toDto(state));
    }

    private void handleFormatFailed(ContentState.FormatFailed state) {
        contentRepository.updateState(ContentDto.builder()
                                              .tmdbId(state.getTmdbId())
                                              .contentUuid(state.getContentUuid())
                                              .state(state.name())
                                              .errorCause(state.getCause())
                                              .build());
    }

    private void handleCompleted(ContentState.Completed state) {
        contentRepository.updateState(toDto(state));
        eventPublisher.sendCompleted(state);
    }

    private ContentDto toDto(ContentState state) {
        return ContentDto.builder()
                .tmdbId(state.getTmdbId())
                .contentUuid(state.getContentUuid())
                .state(state.name())
                .build();
    }
}
