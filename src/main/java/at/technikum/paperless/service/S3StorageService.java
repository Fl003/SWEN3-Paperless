package at.technikum.paperless.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService extends FileStorageService {

    private final S3Client s3;

    @Value("${storage.s3.bucket}")
    private String bucket;

    @Override
    public String store(MultipartFile file, String objectKeyHint) {
        final String key = normalizeKey(objectKeyHint);
        final String contentType = Objects.requireNonNullElse(
                file.getContentType(), MediaType.APPLICATION_OCTET_STREAM_VALUE);

        try {
            s3.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
            log.info("Uploaded to S3 bucket={} key={} size={}", bucket, key, file.getSize());
            return key;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload to S3: " + e.getMessage(), e);
        }
    }

    private String normalizeKey(String hint) {
        String k = hint == null ? "" : hint.trim();
        if (k.startsWith("/")) k = k.substring(1);
        if (k.isEmpty()) k = "docs/unnamed";
        if (!k.startsWith("docs/")) k = "docs/" + k;
        return k;
    }
}
