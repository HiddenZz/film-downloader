package org.downloader.feature.progress.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.feature.progress.model.Progress;
import org.downloader.feature.progress.model.ProgressType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;


@AllArgsConstructor
@Repository
@Slf4j
public class RedisProgressRepository implements ProgressRepository {

    private final StringRedisTemplate redis;

    @Override
    public void set(Progress progress, ProgressType type) {
        redis.opsForHash().put(
                headKeyBuilder(type, progress.tmdbId()),
                key(progress),
                String.valueOf(progress.progress())
        );

        redis.expire(headKeyBuilder(type, progress.tmdbId()), Duration.ofHours(1));

        log.info("Formatting progress {}% for {}", progress.progress(), progress.tmdbId());
    }

    private String headKeyBuilder(ProgressType type, long tmdbId) {
        return "progress:%s:%s".formatted(type.name(), tmdbId);
    }


    private String key(Progress progress) {
        return "%s:%s".formatted(progress.contentUuid(), progress.quality().orElse("none"));
    }
}
