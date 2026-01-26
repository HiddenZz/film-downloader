package org.downloader.feature.downloader.service;

import bt.BtClientBuilder;
import bt.runtime.BtClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.core.configuration.properties.BtProperties;
import org.downloader.feature.downloader.model.DownloadTask;
import org.downloader.feature.downloader.model.TorrentTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@ConditionalOnProperty(name = "downloader.name", havingValue = "torrent")
@AllArgsConstructor
@Slf4j
public class TorrentDownloaderService implements DownloaderService {

    final BtClientBuilder btClientBuilder;
    final ProgressService progressService;
    final BtProperties properties;

    @Override
    public void download(DownloadTask data) {
        final TorrentTask.TorrentPayload payload = ((TorrentTask) data).payload();
        final Path tempDir = tempTorrentPath(payload.cacheGuid(), downloadTorrentFile(payload));
        final StateReporterHelper reporter = new StateReporterHelper(progressService, payload, tempDir);

        AtomicBoolean isDownload = new AtomicBoolean(false);

        try {
            reporter.starting();

            final BtClient btClient = btClientBuilder
                    .afterFileDownloaded((torrent, tf, _) -> {
                        isDownload.set(true);
                        reporter.downloadSuccess(torrent, tf);
                    })
                    .stopWhenDownloaded()
                    .torrent(tempDir.toUri().toURL())
                    .build();

            final var f = btClient.startAsync((state) -> {
                if (isDownload.get()) {
                    return;
                }

                reporter.progress(state);
            }, 1000).join();

            if (f != null) {
                log.info("Future {}, type {}", f, f.getClass());
            }
        } catch (Exception e) {
            log.error("Error during download torrent file for error {} task {}", e, data);
        }
    }


    private Path tempTorrentPath(String name, byte[] torrent) {
        try {
            final Path tempPath = Path.of(properties.tempDir());
            Files.write(Files.createTempFile(tempPath, name, ".torrent"), torrent);
            return tempPath;
        } catch (IOException e) {
            throw new RuntimeException("Exception during create temp Torrent Path ");
        }
    }


    private byte[] downloadTorrentFile(TorrentTask.TorrentPayload payload) {
        final RestClient restClient = RestClient.builder().build();

        return restClient.get()
                .uri(payload.link()).retrieve()
                .body(byte[].class);
    }

}
