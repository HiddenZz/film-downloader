package org.downloader.common.configuration.properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("video-formatting")
@Getter
@Setter
public class VideoFormattingProperties {

    private FfmpegConfig ffmpeg;
    private List<PresetConfig> presets;
    private OutputConfig output;

    @PostConstruct
    protected void init() {
        presets = presets.stream().filter(PresetConfig::enabled).toList();
    }

    public record FfmpegConfig(String threads) {
    }

    public record PresetConfig(
            String name, Resolution resolution, String videoBitrate, String audioBitrate,
            String videoCodec, String audioCodec, int crf, String preset, boolean enabled
    ) {
        public record Resolution(int width, int height) {

        }
    }

    public record OutputConfig(String format, boolean faststart, String outputDir) {
    }
}
