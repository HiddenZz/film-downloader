package org.downloader.feature.saver.service;

import io.lindstrom.m3u8.model.MasterPlaylist;
import io.lindstrom.m3u8.model.MediaPlaylist;
import io.lindstrom.m3u8.model.Variant;
import io.lindstrom.m3u8.parser.MasterPlaylistParser;
import io.lindstrom.m3u8.parser.MediaPlaylistParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.feature.progress.model.ContentState;
import org.downloader.feature.progress.service.ContentStateReporter;
import org.downloader.feature.saver.client.MinIoS3Client;
import org.downloader.feature.saver.model.SaveS3Task;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class SaveS3ServiceImpl implements SaveS3Service {

    private final ContentStateReporter stateReporter;
    private final MasterPlaylistParser masterPlaylistParser = new MasterPlaylistParser();
    private final MediaPlaylistParser mediaPlaylistParser = new MediaPlaylistParser();
    private final MinIoS3Client minIoS3Client;
    private final S3Uploader uploader;


    @Override
    public void accept(SaveS3Task task) {
        try (final ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            final Path masterPlaylistPath = Path.of(task.masterPlaylistPath());
            final MasterPlaylist masterPlaylist = masterPlaylistParser.readPlaylist(masterPlaylistPath);

            final ArrayList<CompletableFuture<Void>> uploadsVariants = new ArrayList<>();

            for (Variant variant : masterPlaylist.variants()) {
                final Path playlistPath = masterPlaylistPath.getParent().resolve(variant.uri());
                final MediaPlaylist mediaPlaylist = mediaPlaylistParser.readPlaylist(playlistPath);

                uploadsVariants.addAll(mediaPlaylist
                                               .mediaSegments()
                                               .stream()
                                               .map(segment -> CompletableFuture.runAsync(
                                                       () -> {
                                                           final Path filePath = playlistPath.resolveSibling(segment.uri());
                                                           final String segmentSubPathForS3 = Path.of(variant.uri())
                                                                   .resolveSibling(segment.uri()).toString();
                                                           try {
                                                               uploader.upload(filePath, inputStream -> minIoS3Client.saveSegment(inputStream, task.tmdbId(), task.contentUuid(), segmentSubPathForS3));
                                                           } catch (Exception e) {
                                                               throw new CompletionException("File %s not found for task %s".formatted(filePath, task), e);
                                                           }
                                                       }, virtualExecutor)
                                               )
                                               .toList());

                uploadsVariants.add(CompletableFuture.runAsync(() -> {
                    try {
                        uploader.upload(playlistPath, inputStream -> minIoS3Client.savePlaylist(inputStream, task.tmdbId(), task.contentUuid(), variant.uri()));
                    } catch (Exception e) {
                        throw new CompletionException("File %s not found for task %s".formatted(playlistPath, task), e);
                    }
                }, virtualExecutor));
            }

            CompletableFuture.allOf(uploadsVariants.toArray(new CompletableFuture[0])).exceptionally(_ -> null).join();

            if (uploadsVariants.stream().anyMatch(CompletableFuture::isCompletedExceptionally)) {
                final String unityException = uploadsVariants.stream()
                        .filter(CompletableFuture::isCompletedExceptionally).map(CompletableFuture::exceptionNow)
                        .map((throwable) -> "Message: %s \n Cause %s".formatted(throwable.getMessage(), throwable.getCause()))
                        .collect(Collectors.joining("\n---\n"));
                throw new RuntimeException("When saving files, one or more files were not saved for task %s. Cause: %s".formatted(task, unityException));
            }

            uploader.upload(masterPlaylistPath, inputStream -> minIoS3Client.saveMasterPlaylist(inputStream, task.tmdbId(), task.contentUuid()));

            stateReporter.report(ContentState.Completed.builder().build());
            
        } catch (Exception e) {
            log.error("error during save formatting video to s3", e);
        }
    }
}
