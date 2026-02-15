package org.downloader.feature.progress.repository;

import org.downloader.feature.progress.model.ContentState;
import org.downloader.feature.saver.model.SaveS3Task;

public interface TaskPublishRepository {
    void addFormatting(ContentState.Downloaded downloaded);

    void addSave(SaveS3Task s3task);
}
