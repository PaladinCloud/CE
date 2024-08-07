package com.paladincloud.common.jobs;

import com.paladincloud.common.config.ConfigService;
import com.paladincloud.common.errors.JobException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class JobExecutor {

    // These are expected environment variables which are made available in
    // ConfigService under the 'environment.' section.
    private static final String ASSET_SHIPPER_DONE_SQS_URL = "ASSET_SHIPPER_DONE_SQS_URL";
    private static final String AUTH_API_URL = "AUTH_API_URL";
    private static final String BASE_PALADIN_CLOUD_API_URI = "BASE_PALADIN_CLOUD_API_URI";
    private static final String CONFIG_SERVICE_URL = "CONFIG_URL";
    private static final String CONFIG_SERVICE_CREDENTIALS = "CONFIG_CREDENTIALS";
    private static final List<String> environmentVariables = List.of(ASSET_SHIPPER_DONE_SQS_URL,
        AUTH_API_URL, BASE_PALADIN_CLOUD_API_URI, CONFIG_SERVICE_URL, CONFIG_SERVICE_CREDENTIALS);

    private static final String ALERT_ERROR_PREFIX = "error occurred in";

    // These are additional arguments that are supported:
    //      asset-type-override - A comma separated list of asset types to use, ignoring what's in the database
    //      index-prefix -        The prefix to use for creating test ElasticSearch indexes
    //      omit-done-event -     if 'true', the final SQS done event will NOT be fired.

    // Provides the query to the config service; a default is used if one isn't given.
    private static final String CONFIG_SERVICE_QUERY = "config-query";

    private static final Logger LOGGER = LogManager.getLogger(JobExecutor.class);

    protected Map<String, String> params = new HashMap<>();

    public void run(String jobName, String[] args) {
        LOGGER.info(STR."Starting \{jobName} \{String.join(" ", args)}");
        var status = "";
        long startTime = System.nanoTime();
        try {
            setDefaultParams();
            var envVars = getEnvironmentVariables();
            params.putAll(parseArgs(args));
            validateRequiredFields();
            ConfigService.retrieveConfigProperties(envVars.get(CONFIG_SERVICE_URL),
                envVars.get(CONFIG_SERVICE_CREDENTIALS));
            ConfigService.setProperties("environment.", envVars);
            ConfigService.setProperties("param.", params);
            execute();
            status = "Succeeded";
        } catch (Throwable t) {
            status = "Failed";
            LOGGER.error(STR."\{ALERT_ERROR_PREFIX} \{jobName}:", t);
        }

        long duration = System.nanoTime() - startTime;
        long minutes = TimeUnit.NANOSECONDS.toMinutes(duration);
        long seconds = TimeUnit.NANOSECONDS.toSeconds(duration - TimeUnit.MINUTES.toNanos(minutes));
        long milliseconds = TimeUnit.NANOSECONDS.toMillis(
            duration - TimeUnit.MINUTES.toNanos(minutes) - TimeUnit.SECONDS.toNanos(seconds));
        LOGGER.info("Job status: {}; execution time {}", status,
            "%d:%02d.%04d".formatted(minutes, seconds, milliseconds));
    }

    protected abstract void execute();

    protected abstract List<String> getRequiredFields();

    private void setDefaultParams() {
        params.put(CONFIG_SERVICE_QUERY,
            "select targetName,targetConfig,displayName from cf_Target where domain ='Infra & Platforms'");
    }

    private Map<String, String> parseArgs(String[] args) {
        var map = new HashMap<String, String>();
        for (String arg : args) {
            if (StringUtils.isBlank(arg)) {
                continue;
            }

            var tokens = arg.split("=");
            if (tokens.length < 2) {
                throw new JobException(
                    STR."Argument format incorrect: \{arg}; should be '--name=value");
            }
            var keyTokens = tokens[0].split("--");
            map.put(keyTokens[keyTokens.length - 1], tokens[1]);
        }
        return map;
    }

    private void validateRequiredFields() {
        var missing = new ArrayList<String>();
        getRequiredFields().forEach(field -> {
            if (!params.containsKey(field)) {
                missing.add(field);
            }
        });
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(
                STR."Missing required field(s): \{String.join(", ", missing)}");
        }
    }

    private Map<String, String> getEnvironmentVariables() {
        var envVars = new HashMap<String, String>();
        for (var name : environmentVariables) {
            var value = System.getenv(name);
            if (value != null) {
                envVars.put(name, value);
            }
        }
        return envVars;
    }
}
