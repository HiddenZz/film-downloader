package org.downloader.feature.progress.model;

import lombok.Builder;

import java.util.Optional;


@Builder
public record Progress(long tmdbId, String contentUuid, int progress, String quality) {

}
