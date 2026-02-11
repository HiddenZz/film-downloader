package org.downloader.feature.progress.repository;

import org.downloader.feature.progress.model.Progress;
import org.downloader.feature.progress.model.ProgressType;

public interface ProgressRepository {

    void set(Progress progress, ProgressType type);
}
