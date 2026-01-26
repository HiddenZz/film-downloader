package org.downloader.feature.downloader.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.feature.downloader.model.ContentDto;
import org.downloader.feature.downloader.model.ContentState;
import org.downloader.feature.downloader.repository.ContentRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
@AllArgsConstructor
@Slf4j
public class CommonProgressService implements ProgressService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ContentRepository contentRepository;

    @Override
    public void report(ContentState state) {
        final ContentDto.ContentDtoBuilder builder = ContentDto.builder()
                .tmdbId(state.tmdbId())
                .state(state.name())
                .contentUuid(state.contentUuid());

        switch (state) {
            case ContentState.Progressing s -> builder.progress(s.progress());
            case ContentState.Success s -> builder.minioKey(s.minioUrl());
            default -> {
            }
        }

        if (state instanceof ContentState.Starting(long tmdbId, String contentUuid)) {
            try {
                contentRepository.create(builder.progress(0)
                                                 .build());
            } catch (Exception e) {
                log.error("Error creating content state row error {} uuid {} tmdbId {}", e, contentUuid, tmdbId);
            }
            return;
        }

        contentRepository.updateState(builder.build());

        if (state instanceof ContentState.DownloadSuccess s) {
            sendToFormatting(s);
        }
    }


    void sendToFormatting(ContentState.DownloadSuccess state) {
        try {
            redisTemplate.opsForStream()
                    .add("formatting:stream", Map.of("data", objectMapper.writeValueAsString(state)));

        } catch (Exception e) {
            report(new ContentState.FormattingError(state.tmdbId(), state.contentUuid()));
        }
    }
}
