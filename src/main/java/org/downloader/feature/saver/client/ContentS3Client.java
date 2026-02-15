package org.downloader.feature.saver.client;

import java.io.InputStream;

public interface ContentS3Client {

    void saveMasterPlaylist(InputStream stream, long tmdbId, String contentUuid);

    void savePlaylist(InputStream stream, long tmdbId, String contentUuid, String path);

    void saveSegment(InputStream stream, long tmdbId, String contentUuid, String path);
}