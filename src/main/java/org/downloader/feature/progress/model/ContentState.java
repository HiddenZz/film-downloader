package org.downloader.feature.progress.model;

import lombok.Builder;
import org.downloader.common.utils.Named;

public sealed interface ContentState permits
        ContentState.Downloading,
        ContentState.Downloaded,
        ContentState.DownloadFailed,
        ContentState.Formatting,
        ContentState.Formatted,
        ContentState.FormatFailed,
        ContentState.Completed {

    String contentUuid();

    long tmdbId();

    default String name() {
        return getClass().getAnnotation(Named.class).value();
    }

    @Builder
    @Named("DOWNLOADING")
    record Downloading(long tmdbId, String contentUuid) implements ContentState {
    }

    @Builder
    @Named("DOWNLOADED")
    record Downloaded(long tmdbId, String contentUuid, String contentName, String filePath) implements ContentState {
    }

    @Builder
    @Named("DOWNLOAD_FAILED")
    record DownloadFailed(long tmdbId, String contentUuid, String cause) implements ContentState {
    }

    @Builder
    @Named("FORMATTING")
    record Formatting(long tmdbId, String contentUuid) implements ContentState {
    }

    @Builder
    @Named("FORMATTED")
    record Formatted(long tmdbId, String contentUuid) implements ContentState {
    }

    @Builder
    @Named("FORMAT_FAILED")
    record FormatFailed(long tmdbId, String contentUuid, String cause) implements ContentState {
    }

    @Builder
    @Named("COMPLETED")
    record Completed(long tmdbId, String contentUuid, String minioUrl) implements ContentState {
    }
}
