package org.downloader.feature.downloader.service;

import org.downloader.feature.downloader.model.ContentState;
import org.downloader.feature.downloader.model.DownloadTask;
import org.springframework.stereotype.Service;


public interface ProgressService {
    void report(ContentState state);

}
