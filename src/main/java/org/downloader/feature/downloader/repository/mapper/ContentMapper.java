package org.downloader.feature.downloader.repository.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.downloader.feature.downloader.model.ContentDto;

@Mapper
public interface ContentMapper {

    int insert(@Param("dto") ContentDto dto);

    int updateState(@Param("dto") ContentDto dto);
}
