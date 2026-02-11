package org.downloader.feature.progress.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.feature.progress.model.Progress;
import org.downloader.feature.progress.model.ProgressType;
import org.downloader.feature.progress.repository.ProgressRepository;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class ProgressReporterImpl implements ProgressReporter {

    private final ProgressRepository redisRepository;

    @Override
    public void downloading(Progress progress) {
        try {
            redisRepository.set(progress, ProgressType.DOWNLOAD);
        } catch (Exception e) {
            log.error("Error during publish download progress for content Uuid:%s, tmdbId:%s".formatted(progress.contentUuid(), progress.tmdbId()), e);
        }
    }

    @Override
    public void formatting(Progress progress) {
        try {
            redisRepository.set(progress, ProgressType.FORMATTING);
        } catch (Exception e) {
            log.error("Error during publish formatting progress for content Uuid:%s, tmdbId:%s".formatted(progress.contentUuid(), progress.tmdbId()), e);
        }
    }

}
