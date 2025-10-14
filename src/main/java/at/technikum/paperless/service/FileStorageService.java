package at.technikum.paperless.service;

import at.technikum.paperless.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class FileStorageService {

    @Value( "${storage.location}")
    private String storageLocation;

    @PostConstruct
    public void init() throws IOException {
        // Erstelle das Speicherverzeichnis beim Start, falls es nicht existiert
        Files.createDirectories(Path.of(storageLocation));
    }

    public String store(MultipartFile file) {
        try {
            String uniqueFileName = generateUniqueFileName(file.getOriginalFilename());
            Path targetLocation = Path.of(storageLocation, uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return targetLocation.toString();
        } catch (IOException ex) {
            throw new FileStorageException("could not store file", ex);
        }
    }

    private String generateUniqueFileName(String originalFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String randomString = UUID.randomUUID().toString().substring(0, 8);
        String extension = originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        return timestamp + "_" + randomString + extension;
    }
}
