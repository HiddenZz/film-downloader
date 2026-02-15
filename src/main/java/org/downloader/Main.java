package org.downloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;

@ConfigurationPropertiesScan(basePackages = "org.downloader.common.configuration")
@SpringBootApplication
public class Main {
    public static void main(String[] args) {

        ApplicationContext ctx = SpringApplication.run(Main.class, args);

    }
}

