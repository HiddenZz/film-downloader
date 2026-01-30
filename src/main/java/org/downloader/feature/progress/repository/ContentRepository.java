package org.downloader.feature.progress.repository;

import org.downloader.feature.progress.model.ContentDto;

public interface ContentRepository {

    boolean isExists(String contentUuid);

    void create(ContentDto dto);

    void updateState(ContentDto dto);

}
