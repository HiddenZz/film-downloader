package org.downloader.feature.downloader.service;

import org.downloader.feature.downloader.model.DownloadTask;

import java.io.IOException;

public interface DownloaderService {

    void download(DownloadTask data);
}
