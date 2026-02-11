package org.downloader.feature.progress.service;

import org.downloader.feature.progress.model.ContentState;

public interface ContentStateReporter {
    void report(ContentState state);
}
