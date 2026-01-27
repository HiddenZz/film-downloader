package org.downloader.common.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ExecutorConfiguration {


    @Bean
    ExecutorService executorService() {

        return new ThreadPoolExecutor(
                5, 5, 0L, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(10),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }


}
