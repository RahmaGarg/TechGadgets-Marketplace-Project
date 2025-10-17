package com.example.demo.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dtos.CanReviewResponse;
import com.example.demo.dtos.CreateReviewRequest;
import com.example.demo.dtos.ProductReviewStats;
import com.example.demo.dtos.ReviewResponse;
import com.example.demo.dtos.UpdateReviewRequest;
import com.example.demo.entities.Review;
import com.example.demo.exceptions.InvalidFileException;
import com.example.demo.exceptions.ReviewAlreadyExistsException;
import com.example.demo.exceptions.ReviewNotFoundException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.repositories.ReviewRepository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {
    
    private final Path fileStorageLocation;
    private static final List<String> ALLOWED_EXTENSIONS = 
        Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    
    @Autowired
    public FileStorageService(@Value("${app.upload.dir}") String uploadDir) throws IOException {
        this.fileStorageLocation = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
        
        Files.createDirectories(this.fileStorageLocation);
    }
    
    public String storeFile(MultipartFile file) throws IOException {
        // Validation du fichier
        validateFile(file);
        
        // Générer un nom unique
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + "." + extension;
        
        // Copier le fichier
        Path targetLocation = this.fileStorageLocation.resolve(newFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("Fichier sauvegardé: {}", newFilename);
        return newFilename;
    }
    
    public void deleteFile(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
            log.info("Fichier supprimé: {}", filename);
        } catch (IOException e) {
            log.error("Erreur lors de la suppression du fichier: {}", filename, e);
        }
    }
    
    public Resource loadFileAsResource(String filename) throws IOException {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("Fichier non trouvé: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("Fichier non trouvé: " + filename);
        }
    }
    
    private void validateFile(MultipartFile file) {
        // Vérifier si le fichier est vide
        if (file.isEmpty()) {
            throw new InvalidFileException("Le fichier est vide");
        }
        
        // Vérifier la taille
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException("Le fichier ne doit pas dépasser 5MB");
        }
        
        // Vérifier l'extension
        String filename = file.getOriginalFilename();
        String extension = getFileExtension(filename);
        
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new InvalidFileException(
                "Type de fichier non autorisé. Extensions acceptées: " + 
                String.join(", ", ALLOWED_EXTENSIONS));
        }
        
        // Vérifier le content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileException("Seules les images sont acceptées");
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            throw new InvalidFileException("Nom de fichier invalide");
        }
        
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new InvalidFileException("Le fichier doit avoir une extension");
        }
        
        return filename.substring(lastDotIndex + 1);
    }
}
