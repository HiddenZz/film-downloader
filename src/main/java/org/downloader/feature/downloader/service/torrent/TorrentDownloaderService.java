package org.downloader.feature.downloader.service.torrent;

import bt.BtClientBuilder;
import bt.runtime.BtClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.common.configuration.properties.BtProperties;
import org.downloader.common.utils.ConditionalOnTorrentProfile;
import org.downloader.feature.downloader.service.DownloaderService;
import org.downloader.feature.downloader.model.torrent.TorrentTask;
import org.downloader.feature.progress.service.ProgressService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@ConditionalOnTorrentProfile
@AllArgsConstructor
@Slf4j
public class TorrentDownloaderService implements DownloaderService<TorrentTask> {

    final BtClientBuilder btClientBuilder;
    final ProgressService progressService;
    final BtProperties properties;
    final RestClient restClient;

    @Override
    public void accept(TorrentTask data) {
        final TorrentTask.TorrentPayload payload = data.payload();
        final Path torrentPath = tempTorrentPath(payload.cacheGuid(), downloadTorrentFile(payload));
        final TorrentStateReporter reporter = new TorrentStateReporter(progressService, payload, torrentPath.getParent());

        try {
            reporter.downloading();

            final BtClient btClient = btClientBuilder
                    .afterFileDownloaded((torrent, tf, _) -> {
                        reporter.downloaded(torrent, tf);
                    })
                    .stopWhenDownloaded()
                    .torrent(torrentPath.toUri().toURL())
                    .build();

            btClient.startAsync(reporter::progress, 1000).join();

        } catch (Exception e) {
            log.error("Error during download torrent file for error {} task {}", e, data);
        }
    }


    private Path tempTorrentPath(String name, byte[] torrent) {
        try {
            return Files.write(Files.createTempFile(Path.of(properties.tempDir()), name, ".torrent"), torrent);
        } catch (IOException e) {
            throw new RuntimeException("Exception during create temp Torrent Path ");
        }
    }


    private byte[] downloadTorrentFile(TorrentTask.TorrentPayload payload) {
        
        return restClient.get()
                .uri(payload.link()).retrieve()
                .body(byte[].class);
    }

}
