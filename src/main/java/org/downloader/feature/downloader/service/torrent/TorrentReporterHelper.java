package org.downloader.feature.downloader.service.torrent;

import bt.metainfo.Torrent;
import bt.metainfo.TorrentFile;
import bt.torrent.TorrentSessionState;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.feature.downloader.model.torrent.TorrentTask;
import org.downloader.feature.progress.model.ContentState;
import org.downloader.feature.progress.model.Progress;
import org.downloader.feature.progress.service.ContentStateReporter;
import org.downloader.feature.progress.service.ContentStateReporterImpl;
import org.downloader.feature.progress.service.ProgressReporter;

import java.nio.file.Path;

@Slf4j
@AllArgsConstructor
public class TorrentReporterHelper {

    private final ProgressReporter progressReporter;
    private final ContentStateReporter contentStateReporter;
    private final TorrentTask.TorrentPayload payload;
    private final Path tempDir;

    void downloading() {
        contentStateReporter.report(ContentState.Downloading.builder()
                                            .tmdbId(payload.tmdbId())
                                            .contentUuid(payload.cacheGuid())
                                            .build());
    }

    void progress(TorrentSessionState state) {
        long total = state.getDownloaded() + state.getLeft();
        int progress = total > 0 ? (int) ((state.getDownloaded() * 100.0) / total) : 0;

        log.info("Task: {} | progress: {}% | seeders: {} | peers: {}",
                 payload.cacheGuid().substring(0, 5), progress, payload.seeders(), state.getConnectedPeers());

        progressReporter.downloading(Progress.builder().tmdbId(payload.tmdbId()).contentUuid(payload.cacheGuid())
                                             .progress(progress).build());
    }

    void downloaded(Torrent torrent, TorrentFile tf) {
        Path fullPath = tempDir;
        for (String el : tf.getPathElements()) {
            fullPath = fullPath.resolve(el);
        }

        contentStateReporter.report(ContentState.Downloaded.builder()
                                            .contentUuid(payload.cacheGuid())
                                            .contentName(torrent.getName())
                                            .filePath(fullPath.toString())
                                            .tmdbId(payload.tmdbId())
                                            .build());
    }

    void downloadFailed(String cause) {
        contentStateReporter.report(ContentState.Failed.builder()
                                            .tmdbId(payload.tmdbId())
                                            .contentUuid(payload.cacheGuid())
                                            .cause(cause)
                                            .build());
    }
}
