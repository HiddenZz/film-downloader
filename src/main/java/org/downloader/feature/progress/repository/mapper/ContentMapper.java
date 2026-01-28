package org.downloader.feature.progress.repository.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.downloader.feature.progress.model.ContentDto;

@Mapper
public interface ContentMapper {

    int insert(@Param("dto") ContentDto dto);

    int updateState(@Param("dto") ContentDto dto);
}
