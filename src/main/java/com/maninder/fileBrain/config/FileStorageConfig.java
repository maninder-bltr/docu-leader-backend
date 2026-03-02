package com.maninder.fileBrain.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig {

    @Value("${file.storage-dir}")
    private String storageDir;

    @Bean
    public Path fileStoragePath() throws IOException {
        Path path = Paths.get(storageDir);
        Files.createDirectories(path);
        return path;
    }
}
