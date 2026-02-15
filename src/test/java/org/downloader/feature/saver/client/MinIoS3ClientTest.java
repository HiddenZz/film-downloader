package org.downloader.feature.saver.client;

import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import org.downloader.common.configuration.properties.S3StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinIoS3ClientTest {

    private static final String BUCKET = "content-bucket";
    private static final String HLS_CONTENT_TYPE = "application/vnd.apple.mpegurl";
    @Mock
    private MinioClient minioClient;
    @Mock
    private ObjectWriteResponse writeResponse;
    @Captor
    private ArgumentCaptor<PutObjectArgs> putObjectArgsCaptor;
    private MinIoS3Client minIoS3Client;

    @BeforeEach
    void setUp() {
        S3StorageProperties properties = new S3StorageProperties(BUCKET, HLS_CONTENT_TYPE);
        minIoS3Client = new MinIoS3Client(properties, minioClient);
    }

    @Test
    void saveMasterPlaylist_shouldUploadWithCorrectBucketAndObject() throws Exception {
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(writeResponse);
        InputStream stream = new ByteArrayInputStream("master content".getBytes());

        minIoS3Client.saveMasterPlaylist(stream, 42L, "uuid-123");

        verify(minioClient).putObject(putObjectArgsCaptor.capture());
        PutObjectArgs args = putObjectArgsCaptor.getValue();

        assertEquals(BUCKET, args.bucket());
        assertEquals("42/uuid-123/master.m3u8", args.object());
        assertEquals(HLS_CONTENT_TYPE, args.contentType());
    }

    @Test
    void savePlaylist_shouldUploadWithCorrectPath() throws Exception {
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(writeResponse);
        InputStream stream = new ByteArrayInputStream("playlist content".getBytes());

        minIoS3Client.savePlaylist(stream, 42L, "uuid-123", "720p/playlist.m3u8");

        verify(minioClient).putObject(putObjectArgsCaptor.capture());
        PutObjectArgs args = putObjectArgsCaptor.getValue();

        assertEquals(BUCKET, args.bucket());
        assertEquals("42/uuid-123/720p/playlist.m3u8", args.object());
        assertEquals(HLS_CONTENT_TYPE, args.contentType());
    }

    @Test
    void saveSegment_shouldUploadWithCorrectPath() throws Exception {
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(writeResponse);
        InputStream stream = new ByteArrayInputStream("segment data".getBytes());

        minIoS3Client.saveSegment(stream, 42L, "uuid-123", "720p/segment0.ts");

        verify(minioClient).putObject(putObjectArgsCaptor.capture());
        PutObjectArgs args = putObjectArgsCaptor.getValue();

        assertEquals(BUCKET, args.bucket());
        assertEquals("42/uuid-123/720p/segment0.ts", args.object());
    }

    @Test
    void saveSegment_shouldNotSetHlsContentType() throws Exception {
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(writeResponse);
        InputStream stream = new ByteArrayInputStream("segment data".getBytes());

        minIoS3Client.saveSegment(stream, 42L, "uuid-123", "720p/segment0.ts");

        verify(minioClient).putObject(putObjectArgsCaptor.capture());
        PutObjectArgs args = putObjectArgsCaptor.getValue();
        assertNotEquals(HLS_CONTENT_TYPE, args.contentType());
    }

    @Test
    void saveMasterPlaylist_shouldThrowRuntimeExceptionOnMinioError() throws Exception {
        when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(new RuntimeException("connection refused"));
        InputStream stream = new ByteArrayInputStream("data".getBytes());

        RuntimeException ex = assertThrows(RuntimeException.class,
                                           () -> minIoS3Client.saveMasterPlaylist(stream, 1L, "uuid"));
        assertTrue(ex.getMessage().contains("Failed save master playlist"));
    }

    @Test
    void savePlaylist_shouldThrowRuntimeExceptionOnMinioError() throws Exception {
        when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(new RuntimeException("timeout"));
        InputStream stream = new ByteArrayInputStream("data".getBytes());

        RuntimeException ex = assertThrows(RuntimeException.class,
                                           () -> minIoS3Client.savePlaylist(stream, 1L, "uuid", "720p/playlist.m3u8"));
        assertTrue(ex.getMessage().contains("Failed save master playlist"));
    }

    @Test
    void saveSegment_shouldThrowRuntimeExceptionOnMinioError() throws Exception {
        when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(new IOException("timeout"));
        InputStream stream = new ByteArrayInputStream("data".getBytes());

        RuntimeException ex = assertThrows(RuntimeException.class,
                                           () -> minIoS3Client.saveSegment(stream, 1L, "uuid", "720p/segment0.ts"));
        assertTrue(ex.getMessage().contains("Failed save master playlist"));
    }
}
