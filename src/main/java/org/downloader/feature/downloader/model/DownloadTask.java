package org.downloader.feature.downloader.model;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TorrentTask.class, name = "torrent")
})
public sealed interface DownloadTask permits TorrentTask {
    String type();
}
