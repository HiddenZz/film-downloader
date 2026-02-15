package org.downloader.feature.downloader.service.torrent;

import bt.BtClientBuilder;
import bt.runtime.BtClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.common.configuration.properties.BtProperties;
import org.downloader.common.utils.ConditionalOnTorrentProfile;
import org.downloader.feature.downloader.service.DownloaderService;
import org.downloader.feature.downloader.model.torrent.TorrentTask;
import org.downloader.feature.progress.service.ContentStateReporter;
import org.downloader.feature.progress.service.ContentStateReporterImpl;
import org.downloader.feature.progress.service.ProgressReporter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@ConditionalOnTorrentProfile
@AllArgsConstructor
@Service
@Slf4j
public class TorrentDownloaderService implements DownloaderService<TorrentTask> {

    final BtClientBuilder btClientBuilder;
    final ContentStateReporter contentStateReporter;
    final ProgressReporter progressReporter;
    final BtProperties properties;
    final RestClient restClient;


    @Override
    public void accept(TorrentTask data) {
        final TorrentTask.TorrentPayload payload = data.payload();
        final Path torrentPath = tempTorrentPath(payload.cacheGuid(), downloadTorrentFile(payload));
        final TorrentReporterHelper reporter = new TorrentReporterHelper(progressReporter, contentStateReporter, payload, torrentPath.getParent());

        try {
            reporter.downloading();

            final BtClient btClient = btClientBuilder
                    .afterFileDownloaded((torrent, tf, _) -> reporter.downloaded(torrent, tf))
                    .stopWhenDownloaded()
                    .torrent(torrentPath.toUri().toURL())
                    .build();

            btClient.startAsync(reporter::progress, 1000).join();

        } catch (Exception e) {
            log.error("Error during download torrent file for error {} task {}", e, data);
            reporter.downloadFailed(e.getMessage());
        }
    }


    private Path tempTorrentPath(String name, byte[] torrent) {
        try {
            Files.createDirectories(Path.of(properties.tempDir()));
            return Files.write(Path.of(properties.tempDir(), "%s.%s".formatted(name, "torrent")), torrent, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new RuntimeException("Exception during create temp Torrent Path ", e);
        }
    }


    private byte[] downloadTorrentFile(TorrentTask.TorrentPayload payload) {
        try {
            return restClient.get()
                    .uri(payload.link()).retrieve()
                    .body(byte[].class);
        } catch (Exception e) {
            throw new RuntimeException("Error downloading torrent file", e);
        }
    }

}
