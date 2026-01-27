package org.downloader.feature.downloader.model;

import lombok.Builder;

public sealed interface ContentState permits ContentState.Starting, ContentState.Progressing, ContentState.Formatting, ContentState.FormattingError, ContentState.DownloadError, ContentState.Success, ContentState.DownloadSuccess {

    String contentUuid();

    long tmdbId();

    default String name() {
        return switch (this) {
            case Starting _ -> "STARTING";
            case Progressing _ -> "PROGRESSING";
            case DownloadSuccess _ -> "DOWNLOAD_SUCCESS";
            case Formatting _ -> "FORMATTING";
            case FormattingError _ -> "FORMATTING_ERROR";
            case DownloadError _ -> "DOWNLOAD_ERROR";
            case Success _ -> "SUCCESS";
        };
    }
    

    @Builder
    record Starting(long tmdbId, String contentUuid) implements ContentState {
    }

    @Builder
    record Progressing(int progress, long tmdbId, String contentUuid) implements ContentState {

    }

    @Builder
    record DownloadSuccess(long tmdbId, String contentUuid, String contentName, String filePath
    ) implements ContentState {

    }

    @Builder
    record Formatting(long tmdbId, String contentUuid) implements ContentState {

    }

    @Builder
    record FormattingError(long tmdbId, String contentUuid) implements ContentState {

    }

    @Builder
    record DownloadError(String cause, long tmdbId, String contentUuid) implements ContentState {

    }

    @Builder
    record Success(String minioUrl, long tmdbId, String contentUuid) implements ContentState {

    }
}
