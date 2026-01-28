package org.downloader.feature.downloader.service;

import bt.metainfo.Torrent;
import bt.metainfo.TorrentFile;
import bt.torrent.TorrentSessionState;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.feature.progress.model.ContentState;
import org.downloader.feature.downloader.model.TorrentTask;
import org.downloader.feature.progress.service.ProgressService;

import java.nio.file.Path;

@Slf4j
@AllArgsConstructor
public class StateReporterHelper {

    final private ProgressService progressService;
    final private TorrentTask.TorrentPayload payload;
    final private Path tempDir;

    void starting() {
        progressService.report(ContentState.Starting.builder()
                                       .contentUuid(payload.cacheGuid())
                                       .tmdbId(1)
                                       .build());
    }

    void progress(TorrentSessionState state) {
        long total = state.getDownloaded() + state.getLeft();
        int progress = total > 0 ? (int) ((state.getDownloaded() * 100.0) / total) : 0;

        log.info("Task: {} | progress: {}% | seeders: {} | peers: {}",
                 payload.cacheGuid().substring(0, 5), progress, payload.seeders(), state.getConnectedPeers());

        progressService.report(ContentState.Progressing.builder()
                                       .progress(progress)
                                       .tmdbId(1)
                                       .contentUuid(payload.cacheGuid())
                                       .build());
    }


    void downloadSuccess(Torrent torrent, TorrentFile tf) {
        Path fullPath = tempDir;
        for (String el : tf.getPathElements()) {
            fullPath = fullPath.resolve(el);
        }

        progressService.report(ContentState.DownloadSuccess.builder()
                                       .contentUuid(payload.cacheGuid())
                                       .contentName(torrent.getName())
                                       .filePath(fullPath.toString())
                                       .tmdbId(1)
                                       .build());
    }

}
