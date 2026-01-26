package org.downloader.feature.downloader.repository;

import org.downloader.feature.downloader.model.ContentDto;

public interface ContentRepository {

    void create(ContentDto dto);

    void updateState(ContentDto dto);

}
