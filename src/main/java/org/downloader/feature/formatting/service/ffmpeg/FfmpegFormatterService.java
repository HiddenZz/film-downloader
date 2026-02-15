package org.downloader.feature.formatting.service.ffmpeg;

import lombok.AllArgsConstructor;
import lombok.Builder;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
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

            final Path outputDir = Files.createDirectories(inputPath.resolveSibling("%s/".formatted(task.contentUuid())));

            final FFmpegProbeResult probe = ffprobe.probe(inputPath.toString());

            final List<CompletableFuture<CompletedJob>> jobs = formattingProperties.getPresets().stream()
                    .filter((preset) -> probe.getStreams().stream()
                            .filter((stream) -> stream.codec_type == FFmpegStream.CodecType.VIDEO)
                            .anyMatch((stream) -> stream.height >= preset.resolution().height()))
                    .map((preset) -> {
                             try {
                                 final Path outputByPresetDir = Files.createDirectories(outputDir.resolve("%s".formatted(preset.name())));
                                 return JobRecord
                                         .builder()
                                         .config(preset)
                                         .ffmpegBuilder(
                                                 new FFmpegBuilder()
                                                         .setInput(inputPath.toString())
                                                         .overrideOutputFiles(true)
                                                         .addOutput(outputByPresetDir.resolve("%s".formatted("playlist.m3u8"))
                                                                            .toString())
                                                         .setFormat("hls")
                                                         .addExtraArgs("-hls_time", "10")
                                                         .addExtraArgs("-hls_list_size", "0")
                                                         .addExtraArgs("-hls_segment_filename",
                                                                       outputByPresetDir.resolve("segment_%03d.ts")
                                                                               .toString())
                                                         .setVideoCodec(preset.videoCodec())
                                                         .setAudioCodec(preset.audioCodec())
                                                         .setVideoQuality(preset.crf())
                                                         .setPreset(preset.preset())
                                                         .setAudioBitRate(preset.audioBitrate())
                                                         .done())
                                         .build();
                             } catch (Exception e) {
                                 log.error("Exception during create dir for quality: %s for: %s".formatted(preset.name(), task.tmdbId()), e);
                                 return null;
                             }
                         }
                    )
                    .filter(Objects::nonNull)
                    .map((job) -> (Supplier<CompletedJob>) () -> {
                        executor.createJob(job.ffmpegBuilder, progress -> {
                            final int percentComplete = (int) Math.round(Duration.ofNanos(progress.out_time_ns)
                                                                                 .toSeconds() / probe.format.duration * 100);
                            progressReporter.formatting(Progress.builder()
                                                                .quality(job.config.name())
                                                                .tmdbId(task.tmdbId())
                                                                .contentUuid((task.contentUuid()))
                                                                .progress(percentComplete).build());
                        }).run();
                        return CompletedJob.builder()
                                .qualityName(job.config.name())
                                .bandwidth(job.config.audioBitrate() + job.config.videoBitrate())
                                .resolution(job.config.resolution().view())
                                .build();
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


            final List<CompletedJob> completed = jobs.stream()
                    .filter(f -> f.state() == Future.State.SUCCESS)
                    .map(CompletableFuture::resultNow).toList();

            if (completed.isEmpty()) {
                throw new RuntimeException("none of formatting tasks were completed successfully");
            }

            contentStateReporter.report(ContentState.Formatted.builder()
                                                .tmdbId(task.tmdbId())
                                                .contentUuid(task.contentUuid())
                                                .masterPlaylistPath(generateMasterPlaylist(outputDir, completed).toString())
                                                .build());


        } catch (Exception e) {
            log.error("Error during format video name {}", task.contentName(), e);

            contentStateReporter.report(ContentState.Failed.builder()
                                                .tmdbId(task.tmdbId())
                                                .contentUuid(task.contentUuid())
                                                .cause("All formating jobs completed with message %s".formatted(e.getMessage()))
                                                .build());
        }
    }


    Path generateMasterPlaylist(Path outputDir, List<CompletedJob> jobs) throws IOException {
        StringBuilder playlist = new StringBuilder();
        playlist.append("#EXTM3U\n");
        playlist.append("#EXT-X-VERSION:3\n");

        for (CompletedJob job : jobs) {
            playlist.append("#EXT-X-STREAM-INF:BANDWIDTH=%d,RESOLUTION=%s\n"
                                    .formatted(job.bandwidth(), job.resolution()));
            playlist.append("%s/playlist.m3u8\n".formatted(job.qualityName()));
        }

        return Files.writeString(outputDir.resolve("master.m3u8"), playlist.toString());
    }

    @Builder
    record JobRecord(VideoFormattingProperties.PresetConfig config, FFmpegBuilder ffmpegBuilder) {
    }

    @Builder
    record CompletedJob(String qualityName, long bandwidth, String resolution) {
    }

}
