package at.technikum.paperless.genaiworker.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class S3StorageClient implements StorageService {
    private final S3Client s3Client;
    @Value("${storage.s3.bucket}")
    private String bucket;

    @Override
    public byte[] load(String key) {
        try (var is = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build())) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load from S3: " + key, e);
        }
    }

    @Override
    public String getContentType(String key) {
        return s3Client.headObject(HeadObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build())
                .contentType();
    }
}
