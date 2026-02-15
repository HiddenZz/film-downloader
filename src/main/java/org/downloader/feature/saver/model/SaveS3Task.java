package org.downloader.feature.saver.model;

import lombok.Builder;

@Builder
public record SaveS3Task(long tmdbId, String contentUuid, String masterPlaylistPath) {
}
