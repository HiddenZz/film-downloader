package org.downloader.feature.downloader.service.torrent;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.common.configuration.properties.BtProperties;
import org.downloader.feature.downloader.service.DownloaderService;
import org.downloader.feature.downloader.model.torrent.TorrentTask;
import org.downloader.feature.progress.model.ContentState;
import org.downloader.feature.progress.service.ProgressService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@AllArgsConstructor
@Slf4j
public class MockTorrentDownloaderService implements DownloaderService<TorrentTask> {

    private final ProgressService progressService;
    private final BtProperties btProperties;

    @Override
    public void accept(TorrentTask data) {
        try {
            final String uuid = "49544514a86aeca26d015b4f542a9c74ebb795b5bcc259da56c3c04a7cd668f8";
            final TorrentTask.TorrentPayload payload = data.payload();

            final Path file = Path.of(btProperties.tempDir(), "test.avi");

            progressService.report(ContentState.Downloaded.builder()
                                           .tmdbId(payload.tmdbId())
                                           .contentUuid(uuid)
                                           .contentName(payload.title())
                                           .filePath(file.toString())
                                           .build());

        } catch (Exception e) {
            log.info("MockTorrentDownloaderService has error", e);
        }
    }
}
