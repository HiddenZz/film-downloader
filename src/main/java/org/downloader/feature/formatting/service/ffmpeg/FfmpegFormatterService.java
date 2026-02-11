package org.downloader.feature.formatting.service.ffmpeg;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.downloader.common.configuration.properties.VideoFormattingProperties;
import org.downloader.common.exceptions.ConversionSourceNotFoundException;
import org.downloader.feature.formatting.model.FormattingTask;
import org.downloader.feature.formatting.service.FormatterService;
import org.downloader.feature.progress.model.ContentState;
import org.downloader.feature.progress.model.Progress;
import org.downloader.feature.progress.service.ContentStateReporter;
import org.downloader.feature.progress.service.ProgressReporter;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class FfmpegFormatterService implements FormatterService {

    private final FFprobe ffprobe;
    private final FFmpegExecutor executor;
    private final VideoFormattingProperties formattingProperties;
    private final ContentStateReporter contentStateReporter;
    private final ProgressReporter progressReporter;

    @Override
    public void accept(FormattingTask task) {
        try {
            final Path inputPath = Path.of(task.filePath());

            if (!Files.isRegularFile(inputPath)) {
                throw new ConversionSourceNotFoundException(inputPath);
            }

            final FFmpegProbeResult probe = ffprobe.probe(inputPath.toString());

            final List<CompletableFuture<String>> jobs = formattingProperties.getPresets().stream()
                    .filter((preset) -> probe.getStreams().stream()
                            .filter((stream) -> stream.codec_type == FFmpegStream.CodecType.VIDEO)
                            .anyMatch((stream) -> stream.height >= preset.resolution().height()))
                    .map((preset) -> {
                             final String outputPath = inputPath.resolveSibling(buildOutputFileName(task, preset.resolution()
                                     .height())).toString();
                             return new JobRecord(preset.name(), outputPath, new FFmpegBuilder()
                                     .setInput(inputPath.toString())
                                     .overrideOutputFiles(true)
                                     .addOutput(outputPath)
                                     .setVideoCodec(preset.videoCodec())
                                     .setAudioCodec(preset.audioCodec())
                                     .setVideoQuality(preset.crf())
                                     .setPreset(preset.preset())
                                     .setAudioBitRate(preset.audioBitrate())
                                     .done());
                         }
                    )
                    .map((job) -> (Supplier<String>) () -> {
                        executor.createJob(job.builder, progress -> {
                            final int percentComplete = (int) Math.round(Duration.ofNanos(progress.out_time_ns)
                                                                                 .toSeconds() / probe.format.duration * 100);
                            progressReporter.formatting(Progress.builder()
                                                                .quality(Optional.of(job.qualityPreset))
                                                                .tmdbId(task.tmdbId())
                                                                .contentUuid((task.contentUuid()))
                                                                .progress(percentComplete).build());
                        }).run();
                        return job.outputPath;
                    })
                    .map(CompletableFuture::supplyAsync)
                    .toList();

            CompletableFuture.allOf(jobs.toArray(new CompletableFuture<?>[0])).exceptionally(_ -> null).join();

            final List<Throwable> throwables = jobs.stream().filter(CompletableFuture::isCompletedExceptionally)
                    .map(CompletableFuture::exceptionNow).toList();

            if (!throwables.isEmpty() && throwables.size() == jobs.size()) {
                final String unityException = throwables.stream()
                        .map((throwable) -> "Message: %s \n Cause %s".formatted(throwable.getMessage(), throwable.getCause()))
                        .collect(Collectors.joining("\n---\n"));
                throw new RuntimeException(unityException);
            }

            jobs.stream()
                    .filter(f -> f.state() == Future.State.SUCCESS)
                    .map(CompletableFuture::resultNow).findFirst()
                    .ifPresent((outputPath) -> {
                        try {
                            contentStateReporter.report(ContentState.Completed.builder()
                                                                .tmdbId(task.tmdbId())
                                                                .contentUuid(task.contentUuid())
                                                                .filePath(outputPath)
                                                                .build());
                        } catch (RuntimeException e) {
                            log.error("Error during save progress formatted video {}", task.contentName(), e);
                        }
                    });


        } catch (Exception e) {
            log.error("Error during format video name {}", task.contentName(), e);

            contentStateReporter.report(ContentState.FormatFailed.builder()
                                                .tmdbId(task.tmdbId())
                                                .contentUuid(task.contentUuid())
                                                .cause("All formating jobs completed with message %s".formatted(e.getMessage()))
                                                .build());
        }
    }


    private String buildOutputFileName(FormattingTask task, int height) {
        return "%s-%s.%s".formatted(height, task.contentName(), formattingProperties.getOutput().format());
    }

    record JobRecord(String qualityPreset, String outputPath, FFmpegBuilder builder) {
    }

}
