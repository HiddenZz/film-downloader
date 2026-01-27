package org.downloader.feature.downloader.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.common.configuration.properties.BtProperties;
import org.downloader.feature.downloader.model.ContentState;
import org.downloader.feature.downloader.model.DownloadTask;
import org.downloader.feature.downloader.model.TorrentTask;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@Primary
@AllArgsConstructor
@Slf4j
public class MockTorrentDownloaderService implements DownloaderService {

    private final ProgressService progressService;
    private final BtProperties btProperties;


    @Override
    public void download(DownloadTask data) {
        try {
            final String uuid = "49544514a86aeca26d015b4f542a9c74ebb795b5bcc259da56c3c04a7cd668f8";
            final TorrentTask.TorrentPayload payload = ((TorrentTask) data).payload();

            final Path file = Path.of(btProperties.tempDir(), "test.avi");

            progressService.report(ContentState.Progressing.builder()
                                           .progress(100)
                                           .tmdbId(1)
                                           .contentUuid(uuid)
                                           .build());

            progressService.report(ContentState.DownloadSuccess.builder()
                                           .tmdbId(1)
                                           .contentUuid(uuid)
                                           .contentName(payload.title())
                                           .filePath(file.toString())
                                           .build());


        } catch (Exception e) {
            log.info("MockTorrentDownloaderService has error", e);
        }
    }
}
