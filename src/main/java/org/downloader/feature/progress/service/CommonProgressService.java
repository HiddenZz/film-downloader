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
public class CommonProgressService implements ProgressService {

    private final ContentRepository contentRepository;
    private final ContentEventPublisher eventPublisher;

    @Override
    public void report(ContentState state) {
        switch (state) {
            case ContentState.Starting s -> handleStarting(s);
            case ContentState.Progressing s -> handleProgress(s);
            case ContentState.DownloadSuccess s -> handleDownloadSuccess(s);
            case ContentState.Success s -> handleSuccess(s);
            case ContentState.DownloadError s -> handleError(s);
            case ContentState.Formatting s -> handleFormatting(s);
            case ContentState.FormattingError s -> handleFormattingError(s);
        }
    }

    private void handleStarting(ContentState.Starting state) {
        contentRepository.create(ContentDto.builder()
                                         .tmdbId(state.tmdbId())
                                         .contentUuid(state.contentUuid())
                                         .state(state.name())
                                         .progress(0)
                                         .build());
    }

    private void handleProgress(ContentState.Progressing state) {
        contentRepository.updateState(ContentDto.builder()
                                              .tmdbId(state.tmdbId())
                                              .contentUuid(state.contentUuid())
                                              .state(state.name())
                                              .progress(state.progress())
                                              .build());
    }

    private void handleDownloadSuccess(ContentState.DownloadSuccess state) {
        contentRepository.updateState(toDto(state));
        eventPublisher.sendToFormatting(state);
    }

    private void handleSuccess(ContentState.Success state) {
        contentRepository.updateState(toDto(state));
        eventPublisher.sendCompleted(state);
    }

    private void handleError(ContentState.DownloadError state) {
        contentRepository.updateState(ContentDto.builder()
                                              .tmdbId(state.tmdbId())
                                              .contentUuid(state.contentUuid())
                                              .state(state.name())
                                              .errorCause(state.cause())
                                              .build());
    }

    private void handleFormatting(ContentState.Formatting state) {
        contentRepository.updateState(toDto(state));
    }

    private void handleFormattingError(ContentState.FormattingError state) {
        contentRepository.updateState(toDto(state));
    }

    private ContentDto toDto(ContentState state) {
        return ContentDto.builder()
                .tmdbId(state.tmdbId())
                .contentUuid(state.contentUuid())
                .state(state.name())
                .build();
    }
}
