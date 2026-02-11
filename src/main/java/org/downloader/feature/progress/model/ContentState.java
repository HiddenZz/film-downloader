package org.downloader.feature.progress.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.downloader.common.utils.Name;
import org.downloader.common.utils.Named;

@AllArgsConstructor
@SuperBuilder
@Getter
public abstract sealed class ContentState implements Name permits
        ContentState.Downloading,
        ContentState.Downloaded,
        ContentState.DownloadFailed,
        ContentState.Formatting,
        ContentState.Formatted,
        ContentState.FormatFailed,
        ContentState.Completed {

    private final long tmdbId;
    private final String contentUuid;


    @SuperBuilder
    @Getter
    @Named("DOWNLOADING")
    public static final class Downloading extends ContentState {
    }

    @SuperBuilder
    @Getter
    @Named("DOWNLOADED")
    public static final class Downloaded extends ContentState {
        private final String contentName;
        private final String filePath;
    }

    @SuperBuilder
    @Getter
    @Named("DOWNLOAD_FAILED")
    public static final class DownloadFailed extends ContentState {
        private final String cause;
    }

    @SuperBuilder
    @Getter
    @Named("FORMATTING")
    public static final class Formatting extends ContentState {
    }

    @SuperBuilder
    @Getter
    @Named("FORMATTED")
    public static final class Formatted extends ContentState {
    }

    @SuperBuilder
    @Getter
    @Named("FORMAT_FAILED")
    public static final class FormatFailed extends ContentState {
        private final String cause;
    }

    @SuperBuilder
    @Getter
    @Named("COMPLETED")
    public static final class Completed extends ContentState {
        private final String filePath;
    }
}