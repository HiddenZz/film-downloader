package org.downloader.common.utils;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(name = "downloader-listener.name", havingValue = "torrent")
public @interface ConditionalOnTorrentProfile {
}
