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
        ContentState.Formatting,
        ContentState.Formatted,
        ContentState.Failed,
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
    @Named("FORMATTING")
    public static final class Formatting extends ContentState {
    }

    @SuperBuilder
    @Getter
    @Named("FORMATTED")
    public static final class Formatted extends ContentState {
        private final String masterPlaylistPath;
    }


    @SuperBuilder
    @Getter
    @Named("COMPLETED")
    public static final class Completed extends ContentState {

    }

    @SuperBuilder
    @Getter
    @Named("FAILED")
    public static final class Failed extends ContentState {
        private final String cause;
    }
}