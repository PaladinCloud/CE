package com.paladincloud;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.datadog.DatadogConfig;
import io.micrometer.datadog.DatadogMeterRegistry;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class PaladinMetrics {

    private static final Logger logger = LoggerFactory.getLogger(PaladinMetrics.class);
    private static MeterRegistry registry;

    private PaladinMetrics() { }

    public static void initialize(String ...tags) {
        JsonObject secret = getDatadogSecret();

        DatadogConfig config = new DatadogConfig() {
            @Override
            public String apiKey() {
                return secret.get("apiKey").getAsString();
            }

            @Override
            public String applicationKey() {
                return secret.get("applicationKey").getAsString();
            }

            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @Override
            public String get(String k) {
                return null;
            }
        };

        registry = new DatadogMeterRegistry(config, Clock.SYSTEM);

        Tags commonTags = Tags.of(tags);
        if (secret.get("env") != null) {
            commonTags = commonTags.and("env", secret.get("env").getAsString());
        }
        registry.config().commonTags(commonTags);
    }

    public static void close() {
        registry.close();
        registry = null;
    }

    public static void incrementCount(String name, String ...tags) {
        Counter counter = registry.counter(name, tags);
        counter.increment();
    }

    private static JsonObject getDatadogSecret() {
        String secretName = String.format("paladincloud/secret/datadog");
        GetSecretValueResponse secretResponse;
        try (SecretsManagerClient secretsClient = SecretsManagerClient.builder().build()) {
            secretResponse = secretsClient.getSecretValue(
                GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build()
            );
        }

        return (JsonObject) JsonParser.parseString(secretResponse.secretString());

    }
}
