package org.downloader.feature.downloader.service;

import org.downloader.feature.downloader.model.DownloadTask;

import java.util.function.Consumer;

public interface DownloaderService<T extends DownloadTask> extends Consumer<T> {
}
