package org.downloader.common.configuration;

import lombok.AllArgsConstructor;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@AllArgsConstructor
public class FfmpegConfiguration {

    @Bean
    public FFmpeg ffmpeg() {
        try {
            return new FFmpeg();
        } catch (IOException e) {
            throw new BeanCreationException("FFmpeg not found", e);
        }
    }

    @Bean
    public FFprobe ffprobe() {
        try {
            return new FFprobe();
        } catch (IOException e) {
            throw new BeanCreationException("FFmpeg not found", e);
        }
    }

    @Bean
    public FFmpegExecutor ffmpegExecutor(FFmpeg ffmpeg, FFprobe ffprobe) {
        return new FFmpegExecutor(ffmpeg, ffprobe);
    }

}
