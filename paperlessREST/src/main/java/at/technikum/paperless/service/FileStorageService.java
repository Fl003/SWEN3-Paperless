package at.technikum.paperless.service;

import org.springframework.web.multipart.MultipartFile;


public interface FileStorageService {
     // store the given file and return a storage location identifier
    String store(MultipartFile file);
}
