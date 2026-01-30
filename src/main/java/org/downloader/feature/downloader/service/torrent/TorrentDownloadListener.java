package org.downloader.feature.downloader.service.torrent;

import org.downloader.common.configuration.properties.DownloadingEventListenerProperties;
import org.downloader.common.utils.ConditionalOnTorrentProfile;
import org.downloader.feature.downloader.listener.RedisDownloadListener;
import org.downloader.feature.downloader.model.torrent.TorrentTask;
import org.downloader.feature.infrastructure.redis.TaskAllocatorService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@ConditionalOnTorrentProfile
@Component
public class TorrentDownloadListener extends RedisDownloadListener<TorrentTask> {

    public TorrentDownloadListener(StringRedisTemplate redis,
                                   DownloadingEventListenerProperties properties,
                                   TaskAllocatorService taskAllocatorService,
                                   TorrentTaskDeserializer torrentTaskDeserializer,
                                   TorrentDownloaderService torrentDownloaderService) {
        super(redis, properties, taskAllocatorService, torrentTaskDeserializer, torrentDownloaderService);
    }
}
