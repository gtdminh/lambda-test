package net.gtdminh.aws;

import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.SnsException;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.Map;

public class SnsUtils {

    private static final Logger log = LoggerFactory.getLogger(SnsUtils.class);

    private static final SnsUtils INSTANCE = new SnsUtils();

    private final SnsClient snsClient;

    private SnsUtils() {
        this.snsClient = SnsClient.builder()
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    public static SnsUtils getInstance() {
        return INSTANCE;
    }

    public PublishResponse publish(String topicArn, Map<String, String> payload) {
        log.info("Publishing message to SNS topic: {}", topicArn);
        String message = new JSONObject(payload).toString();
        log.debug("Message payload: {}", message);

        PublishRequest publishRequest = PublishRequest.builder()
                .topicArn(topicArn)
                .message(message)
                .build();

        try {
            PublishResponse response = snsClient.publish(publishRequest);
            log.info("Successfully published message with ID: {}", response.messageId());
            return response;
        } catch (SnsException e) {
            log.error("Failed to publish message to SNS topic {}", topicArn, e);
            throw e;
        }
    }

}
