package org.downloader.feature.saver.service;

import org.downloader.feature.saver.client.MinIoS3Client;
import org.downloader.feature.saver.model.SaveS3Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SaveS3ServiceImplTest {

    @Mock
    private MinIoS3Client minIoS3Client;

    @Mock
    private S3Uploader uploader;

    private SaveS3ServiceImpl saveS3Service;

    @BeforeEach
    void setUp() throws Exception {
        saveS3Service = new SaveS3ServiceImpl(minIoS3Client, uploader);

        lenient().doAnswer(invocation -> {
            Path path = invocation.getArgument(0);
            Consumer<InputStream> consumer = invocation.getArgument(1);
            try (InputStream is = Files.newInputStream(path)) {
                consumer.accept(is);
            }
            return null;
        }).when(uploader).upload(any(Path.class), any());
    }

    @Test
    void shouldSaveMasterPlaylistToS3() {
        SaveS3Task task = task("fixtures/master.m3u8");

        saveS3Service.accept(task);

        verify(minIoS3Client).saveMasterPlaylist(any(InputStream.class), eq(1L), eq("test-uuid"));
    }

    @Test
    void shouldSaveAllMediaPlaylistsToS3() {
        SaveS3Task task = task("fixtures/master.m3u8");

        saveS3Service.accept(task);

        verify(minIoS3Client).savePlaylist(any(InputStream.class), eq(1L), eq("test-uuid"), eq("1080p/playlist.m3u8"));
        verify(minIoS3Client).savePlaylist(any(InputStream.class), eq(1L), eq("test-uuid"), eq("720p/playlist.m3u8"));
    }

    @Test
    void shouldSaveAllSegmentsToS3() {
        SaveS3Task task = task("fixtures/master.m3u8");

        saveS3Service.accept(task);

        verify(minIoS3Client, times(4)).saveSegment(any(InputStream.class), eq(1L), eq("test-uuid"), anyString());
    }

    @Test
    void shouldSaveCorrectSegmentPaths() {
        SaveS3Task task = task("fixtures/master.m3u8");

        saveS3Service.accept(task);

        verify(minIoS3Client, times(1)).saveSegment(
                any(InputStream.class),
                eq(1L),
                eq("test-uuid"),
                eq("1080p/segment0.txt")
        );

        verify(minIoS3Client, times(1)).saveSegment(
                any(InputStream.class),
                eq(1L),
                eq("test-uuid"),
                eq("1080p/segment1.txt")
        );

        verify(minIoS3Client, times(1)).saveSegment(
                any(InputStream.class),
                eq(1L),
                eq("test-uuid"),
                eq("720p/segment0.txt")
        );
        verify(minIoS3Client, times(1)).saveSegment(
                any(InputStream.class),
                eq(1L),
                eq("test-uuid"),
                eq("720p/segment1.txt")
        );
    }

    @Test
    void shouldHandleInvalidPlaylistPath() {
        SaveS3Task task = SaveS3Task.builder()
                .tmdbId(1L)
                .contentUuid("test-uuid")
                .masterPlaylistPath("/nonexistent/path/master.m3u8")
                .build();

        assertDoesNotThrow(() -> saveS3Service.accept(task));
        verifyNoInteractions(minIoS3Client);
    }

    @Test
    void shouldNotSaveMasterPlaylistWhenSegmentsFail() throws Exception {
        SaveS3Task task = task("fixtures/master.m3u8");

        doThrow(new RuntimeException("S3 error"))
                .when(uploader).upload(argThat(path -> path.toString().contains("segment0")), any());

        assertDoesNotThrow(() -> saveS3Service.accept(task));
        verify(minIoS3Client, never()).saveMasterPlaylist(any(InputStream.class), anyLong(), anyString());
    }

    private SaveS3Task task(String fixtureRelativePath) {
        return SaveS3Task.builder()
                .tmdbId(1L)
                .contentUuid("test-uuid")
                .masterPlaylistPath(getFixturePath(fixtureRelativePath))
                .build();
    }

    private String getFixturePath(String relativePath) {
        return Objects.requireNonNull(getClass().getClassLoader().getResource(relativePath)).getPath();
    }
}
