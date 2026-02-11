package org.downloader.feature.progress.service;

import org.downloader.feature.progress.model.Progress;

public interface ProgressReporter {
    void downloading(Progress progress);

    void formatting(Progress progress);
}
