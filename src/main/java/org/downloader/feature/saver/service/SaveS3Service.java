package org.downloader.feature.saver.service;

import org.downloader.feature.saver.model.SaveS3Task;

import java.util.function.Consumer;

public interface SaveS3Service extends Consumer<SaveS3Task> {
}
