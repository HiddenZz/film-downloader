package org.downloader.feature.downloader.service.torrent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.downloader.common.utils.ConditionalOnTorrentProfile;
import org.downloader.feature.downloader.service.TaskDeserializer;
import org.downloader.feature.downloader.model.torrent.TorrentTask;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnTorrentProfile
@AllArgsConstructor
@Slf4j
public class TorrentTaskDeserializer implements TaskDeserializer<TorrentTask> {

    private final ObjectMapper objectMapper;

    @Override
    public TorrentTask deserialize(Map<String, String> message) throws JsonProcessingException {
        final String data = message.get("data");
        log.debug("Deserializing torrent task: {}", data);
        if (data == null || data.isEmpty()) {
            return null;
        }
        return objectMapper.readValue(data, TorrentTask.class);
    }
}
