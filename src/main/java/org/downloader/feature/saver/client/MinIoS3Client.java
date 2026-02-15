package org.downloader.feature.saver.client;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.checkerframework.checker.units.qual.A;
import org.downloader.common.configuration.properties.S3StorageProperties;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Path;

@AllArgsConstructor
@Component
@Slf4j
public class MinIoS3Client implements ContentS3Client {

    private final S3StorageProperties s3StorageProperties;
    private final MinioClient minioClient;

    @Override
    public void saveMasterPlaylist(InputStream stream, long tmdbId, String contentUuid) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                                          .bucket(s3StorageProperties.topPrefix())
                                          .object("%s/%s/master.m3u8".formatted(tmdbId, contentUuid))
                                          .stream(stream, -1, 10485760)
                                          .contentType(s3StorageProperties.hlsPlaylistContentType())
                                          .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed save master playlist. Bucket");
        }
    }

    @Override
    public void savePlaylist(InputStream stream, long tmdbId, String contentUuid, String path) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                                          .bucket(s3StorageProperties.topPrefix())
                                          .object(Path.of("%s/%s/%s".formatted(tmdbId, contentUuid, path)).toString())
                                          .stream(stream, -1, 10485760)
                                          .contentType(s3StorageProperties.hlsPlaylistContentType())
                                          .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed save master playlist. Bucket");
        }
    }

    @Override
    public void saveSegment(InputStream stream, long tmdbId, String contentUuid, String path) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                                          .bucket(s3StorageProperties.topPrefix())
                                          .object(Path.of("%s/%s/%s".formatted(tmdbId, contentUuid, path)).toString())
                                          .stream(stream, -1, 10485760)
                                          .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed save master playlist. Bucket");
        }
    }
}
