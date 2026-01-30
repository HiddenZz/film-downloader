package org.downloader.feature.downloader.model.torrent;

import org.downloader.feature.downloader.model.DownloadTask;

public record TorrentTask(TorrentPayload payload) implements DownloadTask {

    @Override
    public String type() {
        return "torrent";
    }

    public record TorrentPayload(
            long tmdbId,
            String cacheGuid,
            String tracker,
            String trackerId,
            String categoryDesc,
            String title,
            String guid,
            String link,
            Long size,
            Integer seeders,
            Integer peers
    ) {
    }
}
