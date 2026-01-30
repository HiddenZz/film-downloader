package org.downloader.feature.downloader.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.downloader.feature.downloader.model.DownloadTask;

import java.util.Map;

public interface TaskDeserializer<T extends DownloadTask> {

    T deserialize(Map<String, String> message) throws JsonProcessingException;
}
