package at.technikum.paperless.genaiworker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class S3Config {
    @Value("${storage.s3.endpoint:http://minio:9000}")
    private String endpoint;

    @Value("${storage.s3.region:us-east-1}")
    private String region;

    @Value("${storage.s3.accessKey:minio}")
    private String accessKey;

    @Value("${storage.s3.secretKey:minio123}")
    private String secretKey;

    @Value("${storage.s3.pathStyle:true}")
    private Boolean pathStyle;

    @Bean
    public S3Client s3Client() {

        S3Client client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(pathStyle)
                                .build()
                )
                .overrideConfiguration(ClientOverrideConfiguration.builder().build())
                .endpointOverride(URI.create(endpoint))  // <<<<<< wichtig: endpoint override für MinIO
                .build();

        return client;
    }
}
