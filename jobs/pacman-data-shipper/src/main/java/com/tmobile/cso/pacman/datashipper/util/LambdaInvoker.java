package com.tmobile.cso.pacman.datashipper.util;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.tmobile.cso.pacman.datashipper.config.CredentialProvider;

public class LambdaInvoker {

    private static final Logger LOGGER = LoggerFactory.getLogger(LambdaInvoker.class);
    private static final String TENANT_CONFIG_TABLE = "tenant-config";
    private static final String LAMBDA_NAME_SUFFIX = "-svc-tagging-compliance-summary-lambda";

    private static String cachedInternalStackName;
    private static AWSLambda lambdaClient;

    private LambdaInvoker() {

    }

    private static String getRegion() {
        return System.getenv("REGION");
    }

    private static String getTenantId() {
        return System.getenv("TENANT_ID");
    }

    private static AWSLambda getLambdaClient() {
        if (lambdaClient == null) {
            String account = System.getProperty("base.account");
            String role = System.getProperty("s3.role");

            lambdaClient = AWSLambdaClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(
                            new CredentialProvider().getCredentials(account, role)))
                    .withRegion(getRegion())
                    .build();
        }
        return lambdaClient;
    }

    public static String getInternalStackName() throws Exception {
        if (cachedInternalStackName != null) {
            return cachedInternalStackName;
        }

        Map<String, String> config = DynamoDBHelper.get(
                getRegion(),
                TENANT_CONFIG_TABLE,
                Constants.TENANT_ID,
                getTenantId(),
                Collections.singletonMap(Constants.INTERNAL_STACK_NAME, Constants.INTERNAL_STACK_NAME)
        );

        cachedInternalStackName = config.get(Constants.INTERNAL_STACK_NAME);
        if (cachedInternalStackName == null || cachedInternalStackName.isEmpty()) {
            throw new Exception("internal_stack_name not found in "
                    + TENANT_CONFIG_TABLE + " for tenant: " + getTenantId());
        }

        LOGGER.info("Retrieved internal_stack_name: {}", cachedInternalStackName);
        return cachedInternalStackName;
    }

    public static String invokeTaggingSummaryLambda(String ag) throws Exception {
        String tenantId = getTenantId();
        String internalStackName = getInternalStackName();
        String lambdaName = internalStackName + LAMBDA_NAME_SUFFIX;

        JsonObject http = new JsonObject();
        http.addProperty("method", "POST");
        http.addProperty("protocol", "HTTP/1.1");

        JsonObject lambdaTenant = new JsonObject();
        lambdaTenant.addProperty("tenantId", tenantId);

        JsonObject authorizer = new JsonObject();
        authorizer.add("lambda", lambdaTenant);

        JsonObject requestContext = new JsonObject();
        requestContext.add("http", http);
        requestContext.add("authorizer", authorizer);

        JsonObject headers = new JsonObject();
        headers.addProperty("accept", "application/json");
        headers.addProperty("content-type", "application/json");

        JsonObject body = new JsonObject();
        body.addProperty("ag", ag);

        JsonObject payload = new JsonObject();
        payload.add("requestContext", requestContext);
        payload.addProperty("rawPath", "api/v2/compliance/tagging-summary");
        payload.add("queryStringParameters", new JsonObject());
        payload.add("headers", headers);
        payload.addProperty("body", body.toString());

        LOGGER.info("Invoking Lambda: {} for ag: {}", lambdaName, ag);

        InvokeRequest invokeRequest = new InvokeRequest()
                .withFunctionName(lambdaName)
                .withPayload(payload.toString());

        InvokeResult result = getLambdaClient().invoke(invokeRequest);
        String rawResponse = new String(result.getPayload().array(), StandardCharsets.UTF_8);

        if (result.getFunctionError() != null) {
            LOGGER.error("Lambda function error: {}, response: {}", result.getFunctionError(), rawResponse);
            throw new Exception("Lambda invocation failed for " + lambdaName + ": " + rawResponse);
        }

        LOGGER.info("Lambda response status code: {}", result.getStatusCode());

        JsonObject responseObj = JsonParser.parseString(rawResponse).getAsJsonObject();
        int statusCode = responseObj.get("statusCode").getAsInt();
        if (statusCode != 200) {
            LOGGER.error("Lambda returned non-200 status: {}, response: {}", statusCode, rawResponse);
            throw new Exception("Lambda returned status " + statusCode + " for " + lambdaName);
        }

        return responseObj.get("body").getAsString();
    }
}