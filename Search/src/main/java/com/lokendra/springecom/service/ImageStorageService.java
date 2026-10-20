package com.lokendra.springecom.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageStorageService {

    @Value("${app.image.upload-dir:uploads/images}")
    private String uploadDir;

    private Path rootLocation;

    
    @PostConstruct
    public void init() throws IOException {
        rootLocation = Paths.get(uploadDir);
        Files.createDirectories(rootLocation);
    }

    
    public String store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store an empty file.");
        }

        String originalName = file.getOriginalFilename();
        String extension    = (originalName != null && originalName.contains("."))
                              ? originalName.substring(originalName.lastIndexOf('.'))
                              : "";

        String storedName = UUID.randomUUID() + extension;
        Path   destination = rootLocation.resolve(storedName).normalize();

        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return storedName;
    }

    
    public byte[] load(String filename) throws IOException {
        Path file = rootLocation.resolve(filename).normalize();
        if (!Files.exists(file)) {
            throw new java.io.FileNotFoundException("Image not found: " + filename);
        }
        return Files.readAllBytes(file);
    }

    
    public String storeBytes(byte[] bytes, String mimeType) throws IOException {
        String extension = switch (mimeType) {
            case "image/jpeg" -> ".jpg";
            case "image/gif"  -> ".gif";
            case "image/webp" -> ".webp";
            default           -> ".png";
        };
        String storedName  = UUID.randomUUID() + extension;
        Path   destination = rootLocation.resolve(storedName).normalize();
        Files.write(destination, bytes);
        return storedName;
    }

    
    public void delete(String filename) {
        if (filename == null || filename.isBlank()) return;
        try {
            Path file = rootLocation.resolve(filename).normalize();
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            
        }
    }
}
