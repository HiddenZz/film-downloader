package org.downloader;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "org.downloader.core.configuration")
public class Main {
    public static void main(String[] args) {

        ApplicationContext ctx = SpringApplication.run(Main.class, args);

    }
}

