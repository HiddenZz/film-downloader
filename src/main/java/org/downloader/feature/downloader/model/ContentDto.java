package org.downloader.feature.downloader.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContentDto {
    long tmdbId;
    String contentUuid;
    String state;
    Integer progress;
    String errorCause;
}
