package at.technikum.paperless.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class S3StorageService implements FileStorageService {

    private final S3Client s3;

    @Value("${storage.s3.bucket}")
    private String bucket;

    @Override
    public String store(MultipartFile file) {
        final String key = buildKey(file);
        final String contentType = Objects.requireNonNullElse(
                file.getContentType(), MediaType.APPLICATION_OCTET_STREAM_VALUE);

        try (var in = file.getInputStream()) {
            s3.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(in, file.getSize())
            );
            log.info("Uploaded to S3 bucket={} key={} size={}", bucket, key, file.getSize());
            return key;
        } catch (IOException e) {
            log.error("Failed to upload to S3", e);
            throw new RuntimeException("Failed to upload to S3", e);
        }
    }

    private String buildKey(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            original = "upload.bin";
        }
        // sanitize filename a bit for S3 keys
        String safeName = original
                .replace("\\", "/")
                .replace("..", ".")
                .getBytes(StandardCharsets.UTF_8).length > 0 ? original : "upload.bin";

        return "docs/" + UUID.randomUUID() + "-" + safeName;
    }

    public byte[] load(String key) {
        GetObjectRequest req = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        try (ResponseInputStream<GetObjectResponse> is = s3.getObject(req)) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from S3 bucket=" + bucket + " key=" + key, e);
        }
    }

    public String getContentType(String key) {
        var head = s3.headObject(HeadObjectRequest.builder()
                .bucket(bucket).key(key).build());
        return head.contentType(); // kann null sein -> unten fallback
    }
}
