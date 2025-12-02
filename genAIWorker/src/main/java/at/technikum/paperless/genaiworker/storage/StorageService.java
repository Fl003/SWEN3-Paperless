package at.technikum.paperless.genaiworker.storage;

public interface StorageService {
    byte[] load(String key);
    String getContentType(String key);
}
