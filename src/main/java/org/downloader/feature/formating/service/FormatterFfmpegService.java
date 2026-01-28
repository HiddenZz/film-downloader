package org.downloader.feature.formating.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.downloader.feature.formating.model.FormattingTask;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@AllArgsConstructor
@Slf4j
public class FormatterFfmpegService implements FormatterService {


    @Override
    public void format(FormattingTask task) {
        try {
            final Path inputPath = Path.of(task.filePath());
            final Path outPutPath = inputPath.resolveSibling("output.mp4");

            final FFmpeg ffmpeg = new FFmpeg();
            FFprobe ffprobe = new FFprobe();

            // TODO: test format config. Replace with format config builder
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(inputPath.toString())
                    .overrideOutputFiles(true)
                    .addOutput(outPutPath.toString())
                    .setVideoCodec("libx264")
                    .addExtraArgs("-preset", "slow")
                    .addExtraArgs("-crf", "18")
                    .setAudioCodec("aac")
                    .setAudioBitRate(192_000)
                    .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

            executor.createJob(builder).run();

        } catch (Exception e) {
            log.error("Error during format video name {}", task.contentName(), e);
        }
    }
}
