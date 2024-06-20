package com.paladincloud.common.jobs;

import com.paladincloud.common.config.ConfigService;
import com.paladincloud.common.errors.JobException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class JobExecutor {

    private static final String CONFIG_SERVICE_URL = "config-url";
    private static final String CONFIG_SERVICE_CREDENTIALS = "config-credentials";
    private static final String CONFIG_SERVICE_QUERY = "config-query";
    private static final Logger LOGGER = LogManager.getLogger(JobExecutor.class);
    private static final List<String> executorRequiredFields = List.of(CONFIG_SERVICE_CREDENTIALS,
        CONFIG_SERVICE_URL);
    protected Map<String, String> params = new HashMap<>();

    public int run(String[] args) {
        LOGGER.info("Starting job executor");
        int status;
        long startTime = System.nanoTime();
        try {
            setDefaultParams();
            params.putAll(parseArgs(args));
            validateRequiredFields();
            ConfigService.retrieveConfigProperties(params.get(CONFIG_SERVICE_URL),
                params.get(CONFIG_SERVICE_CREDENTIALS));
            ConfigService.setProperties("param.", params);
            status = execute();
        } catch (JobException je) {
            status = 1;
            LOGGER.error("Job failed", je);
        } catch (Throwable t) {
            status = -1;
            LOGGER.error("Unexpected job failure:", t);
        }

        long executionMinutes = TimeUnit.MINUTES.convert(System.nanoTime() - startTime,
            TimeUnit.NANOSECONDS);
        LOGGER.info("Job status: {}; execution time: {} minutes", status, executionMinutes);
        return status;
    }

    protected abstract int execute();

    protected abstract List<String> getRequiredFields();

    private void setDefaultParams() {
        params.put(CONFIG_SERVICE_QUERY,
            "select targetName,targetConfig,displayName from cf_Target where domain ='Infra & Platforms'");
    }

    private Map<String, String> parseArgs(String[] args) {
        var map = new HashMap<String, String>();
        for (String arg : args) {
            var tokens = arg.split("=");
            var keyTokens = tokens[0].split("--");
            map.put(keyTokens[keyTokens.length - 1], tokens[1]);
        }
        return map;
    }

    private void validateRequiredFields() {
        var missing = new ArrayList<String>();
        Stream.concat(getRequiredFields().stream(), executorRequiredFields.stream())
            .forEach(field -> {
                if (!params.containsKey(field)) {
                    missing.add(field);
                }
            });
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(
                "Missing required field(s): " + String.join(", ", missing));
        }
    }
}
