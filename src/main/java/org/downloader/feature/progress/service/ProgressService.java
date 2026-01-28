package org.downloader.feature.progress.service;

import org.downloader.feature.progress.model.ContentState;

public interface ProgressService {
    void report(ContentState state);

}
