package com.paladincloud.jobscheduler.schema.jobs_and_rule_scheduling.marshaller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Marshaller {

    // add private constructor to prevent instantiation
    private Marshaller() {
    }

    private static final ObjectMapper MAPPER = createObjectMapper();

    public static <T> void marshal(OutputStream output, T value) throws IOException {
        MAPPER.writeValue(output, value);
    }

    public static <T> T unmarshal(InputStream input, Class<T> type) throws IOException {
        return MAPPER.readValue(input, type);
    }

    private static ObjectMapper createObjectMapper() {
        return new ObjectMapper()
                .findAndRegisterModules()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static <T> String marshal(T value) throws IOException {
        return MAPPER.writeValueAsString(value);
    }
}
