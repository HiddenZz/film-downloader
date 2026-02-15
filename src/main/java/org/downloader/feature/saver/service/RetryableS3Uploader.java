package org.downloader.feature.saver.service;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.function.Consumer;

@Component
public class RetryableS3Uploader implements S3Uploader {


    @Retryable(noRetryFor = NoSuchFileException.class, backoff = @Backoff(delay = 200, random = true))
    @Override
    public void upload(Path path, Consumer<InputStream> uploader) throws NoSuchFileException {
        try (InputStream is = Files.newInputStream(path)) {
            uploader.accept(is);
        } catch (NoSuchFileException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload %s".formatted(path), e);
        }
    }
}
