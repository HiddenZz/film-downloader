package org.downloader.feature.progress.repository;

import lombok.RequiredArgsConstructor;
import org.downloader.feature.progress.model.ContentDto;
import org.downloader.common.exceptions.ContentNotFoundException;
import org.downloader.feature.progress.repository.mapper.ContentMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ContentRepositoryImpl implements ContentRepository {

    private final ContentMapper contentMapper;

    public void create(ContentDto dto) {
        int rows = contentMapper.insert(dto);
        if (rows == 0) {
            throw new IllegalStateException("Failed to create content: " + dto.getContentUuid());
        }
    }

    public void updateState(ContentDto dto) {
        int rows = contentMapper.updateState(dto);
        if (rows == 0) {
            throw new ContentNotFoundException(dto.getContentUuid());
        }
    }
}