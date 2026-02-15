package org.downloader.feature.saver.listener;

import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;

public interface SaveS3ListenerService extends StreamListener<String, MapRecord<String, String, String>> {
}
