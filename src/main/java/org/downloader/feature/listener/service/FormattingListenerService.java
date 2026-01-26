package org.downloader.feature.listener.service;

import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;

public interface FormattingListenerService extends StreamListener<String, MapRecord<String, String, String>> {
}
