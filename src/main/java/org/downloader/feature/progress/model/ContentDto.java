package org.downloader.feature.progress.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContentDto {
    long tmdbId;
    String contentUuid;
    String state;
    String errorCause;
}
