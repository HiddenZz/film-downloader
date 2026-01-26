package org.downloader.core.configuration;

import bt.Bt;
import bt.BtClientBuilder;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import bt.protocol.crypto.EncryptionPolicy;
import bt.runtime.BtClient;
import bt.runtime.BtRuntime;
import bt.runtime.Config;
import bt.torrent.selector.PieceSelector;
import bt.torrent.selector.SequentialSelector;
import org.downloader.core.configuration.properties.BtProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.net.InetAddress;
import java.nio.file.Paths;

@Configuration
@ConditionalOnProperty(name = "downloader.name", havingValue = "torrent")

public class BtConfiguration {


    @Bean
    BtClientBuilder btClientBuilder(BtRuntime btRuntime, BtProperties properties) {
        final Storage storage = new FileSystemStorage(Paths.get(properties.tempDir()));
        final PieceSelector selector = SequentialSelector.sequential();
        return Bt.client(btRuntime)
                .storage(storage).selector(selector);
    }

    @Bean
    BtRuntime btRuntime(Config btConfig, DHTModule dhtModule) {
        return BtRuntime.builder(btConfig).module(dhtModule).autoLoadModules().build();
    }


    @Bean
    DHTModule dhtModule() {
        return new DHTModule(new DHTConfig() {

            @Override
            public boolean shouldUseRouterBootstrap() {
                return true;
            }
        });
    }

    @Bean
    Config btConfig() {
        return new Config() {

            @Override
            public int getNumOfHashingThreads() {
                return Runtime.getRuntime().availableProcessors();
            }

            @Override
            public EncryptionPolicy getEncryptionPolicy() {
                return EncryptionPolicy.REQUIRE_ENCRYPTED;
            }
        };
    }
}
