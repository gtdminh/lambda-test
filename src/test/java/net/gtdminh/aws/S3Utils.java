package net.gtdminh.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class S3Utils {

    private static final Logger log = LoggerFactory.getLogger(S3Utils.class);

    private static final S3Utils INSTANCE = new S3Utils();

    private final S3Client s3Client;

    private S3Utils() {
        this.s3Client = S3Client.builder()
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    public static S3Utils getInstance() {
        return INSTANCE;
    }

    public String getS3ObjectAsString(String bucketName, String key) throws IOException {
        log.info("Getting object from S3: bucket='{}', key='{}'", bucketName, key);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject(getObjectRequest)) {
            String content = new String(s3ObjectStream.readAllBytes(), StandardCharsets.UTF_8);
            log.info("Successfully retrieved object from S3: bucket='{}', key='{}'", bucketName, key);
            return content;
        } catch (S3Exception | IOException e) {
            log.error("Failed to get object from S3: bucket='{}', key='{}'", bucketName, key, e);
            throw e;
        }
    }

    public PutObjectResponse putS3Object(String bucketName, String key, String content) {
        log.info("Putting object to S3: bucket='{}', key='{}'", bucketName, key);
        log.debug("Object content: {}", content);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try {
            PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromString(content));
            log.info("Successfully put object to S3 with ETag: {}", response.eTag());
            return response;
        } catch (S3Exception e) {
            log.error("Failed to put object to S3: bucket='{}', key='{}'", bucketName, key, e);
            throw e;
        }
    }
}
