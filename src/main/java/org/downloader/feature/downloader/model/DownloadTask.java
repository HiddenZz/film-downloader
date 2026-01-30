package org.downloader.feature.downloader.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.downloader.feature.downloader.model.torrent.TorrentTask;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TorrentTask.class, name = "torrent")
})
public interface DownloadTask {
    String type();
}
