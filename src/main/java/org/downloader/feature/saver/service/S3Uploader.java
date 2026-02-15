package org.downloader.feature.saver.service;

import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.function.Consumer;

public interface S3Uploader {

    void upload(Path path, Consumer<InputStream> uploader) throws NoSuchFileException;
}
