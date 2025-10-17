package com.example.demo.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class FileStorageConfig {
    
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    @Bean
    public String uploadDirectory() throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("✅ Dossier d'upload créé: {}", uploadPath);
        } else {
            log.info("✅ Dossier d'upload existe déjà: {}", uploadPath);
        }
        
        // Afficher le chemin absolu pour vérification
        log.info("📁 Chemin absolu d'upload: {}", uploadPath.toAbsolutePath());
        
        return uploadPath.toString();
    }
}
