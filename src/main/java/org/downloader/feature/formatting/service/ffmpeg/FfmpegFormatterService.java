package org.downloader.feature.formatting.service.ffmpeg;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.downloader.common.configuration.properties.VideoFormattingProperties;
import org.downloader.common.exceptions.ConversionSourceNotFoundException;
import org.downloader.feature.formatting.model.FormattingTask;
import org.downloader.feature.formatting.service.FormatterService;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
@AllArgsConstructor
@Slf4j
public class FfmpegFormatterService implements FormatterService {

    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;
    private final FFmpegExecutor executor;
    private final VideoFormattingProperties formattingProperties;

    @Override
    public void accept(FormattingTask task) {
        try {
            final Path inputPath = Path.of(task.filePath());
            final Path outputPath = inputPath.resolveSibling(buildOutputFileName(task));

            if (!Files.isRegularFile(inputPath)) {
                throw new ConversionSourceNotFoundException(inputPath);
            }

            // TODO: use formattingProperties presets
            final FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(inputPath.toString())
                    .overrideOutputFiles(true)
                    .addOutput(outputPath.toString())
                    .setVideoCodec("libx264")
                    .addExtraArgs("-preset", "slow")
                    .addExtraArgs("-crf", "18")
                    .setAudioCodec("aac")
                    .setAudioBitRate(192_000)
                    .done();

            executor.createJob(builder).run();

        } catch (Exception e) {
            log.error("Error during format video name {}", task.contentName(), e);
        }
    }

    private String buildOutputFileName(FormattingTask task) {
        return "%s.%s".formatted(task.contentName(), formattingProperties.getOutput().format());
    }
}
