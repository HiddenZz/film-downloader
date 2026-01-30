package org.downloader.feature.progress.repository.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.downloader.feature.progress.model.ContentDto;

@Mapper
public interface ContentMapper {

    boolean isExists(String contentUuid);

    long insert(@Param("dto") ContentDto dto);

    int updateState(@Param("dto") ContentDto dto);
}
