package org.downloader.common.configuration.properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("video-formatting")
@Getter
public class VideoFormattingProperties {

    private final FfmpegConfig ffmpeg;
    private final List<PresetConfig> presets;
    private final OutputConfig output;

    public VideoFormattingProperties(FfmpegConfig ffmpeg, List<PresetConfig> presets, OutputConfig output) {
        this.ffmpeg = ffmpeg;
        this.presets = presets.stream().filter(PresetConfig::enabled).toList();
        this.output = output;
    }

    public record FfmpegConfig(String threads) {
    }

    public record PresetConfig(
            String name, Resolution resolution, long videoBitrate, long audioBitrate,
            String videoCodec, String audioCodec, int crf, String preset, boolean enabled
    ) {
        public record Resolution(int width, int height) {
            public String view() {
                return "%sx%s".formatted(width, height);
            }
        }
    }

    public record OutputConfig(String format, boolean faststart, String outputDir) {
    }
}
