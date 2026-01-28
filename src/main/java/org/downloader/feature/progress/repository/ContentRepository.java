package org.downloader.feature.progress.repository;

import org.downloader.feature.progress.model.ContentDto;

public interface ContentRepository {

    void create(ContentDto dto);

    void updateState(ContentDto dto);

}
