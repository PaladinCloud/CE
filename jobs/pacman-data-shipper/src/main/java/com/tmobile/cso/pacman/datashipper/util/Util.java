/*******************************************************************************
 * Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * <p>
 *   http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.tmobile.cso.pacman.datashipper.util;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.cso.pacman.datashipper.config.CredentialProvider;
import com.tmobile.cso.pacman.datashipper.config.S3ClientConfig;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

public class Util {
    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    private Util() {
        throw new IllegalStateException("Util is a utility class");
    }

    /**
     * Contains.
     *
     * @param x    the x
     * @param y    the y
     * @param keys the keys
     * @return true, if successful
     */
    public static boolean contains(Map<String, ?> x, Map<String, ?> y, String[] keys) {
        for (String key : keys) {
            if (!x.get(key).equals(y.get(key)))
                return false;
        }
        return true;
    }

    /**
     * Concatenate.
     *
     * @param map       the map
     * @param keys      the keys
     * @param delimiter the delimiter
     * @return the string
     */
    public static String concatenate(Map<String, Object> map, String[] keys, String delimiter) {
        List<String> values = new ArrayList<>();
        for (String key : keys) {
            if (map.get(key) == null) {
                continue;
            }
            values.add(map.get(key).toString());
        }

        return String.join(delimiter, values);
    }

    /**
     * Parses the json.
     *
     * @param json the json
     * @return the map
     */
    public static Map<String, Object> parseJson(String json) {
        try {
            return new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            LOGGER.error("Error in parseJson", e);
        }

        return new HashMap<>();
    }

    /**
     * Gets the unique ID.
     *
     * @param idstring the idstring
     * @return the unique ID
     */
    public static String getUniqueID(String idstring) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            return (new HexBinaryAdapter()).marshal(md5.digest(idstring.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Error in getUniqueID", e);
        }

        return "";
    }

    /**
     * Gets the stack trace.
     *
     * @param e the e
     * @return the stack trace
     */
    public static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * Base 64 decode.
     *
     * @param encodedStr the encoded str
     * @return the string
     */
    public static String base64Decode(String encodedStr) {
        return new String(Base64.getDecoder().decode(encodedStr));
    }

    /**
     * Encode url.
     *
     * @param toBeEncoded the to be encoded
     * @return the string
     */
    public static String encodeUrl(String toBeEncoded) {
        String encoded = toBeEncoded;
        try {
            encoded = URLEncoder.encode(toBeEncoded, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            LOGGER.error("Error in encodeUrl", e1);
        }
        return encoded;
    }

    public static String base64Encode(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    public static Map<String, Object> getHeader(String base64Creds) {
        Map<String, Object> authToken = new HashMap<>();
        authToken.put("Content-Type", ContentType.APPLICATION_JSON.toString());
        authToken.put("Authorization", "Basic " + base64Creds);
        return authToken;
    }

    public static <T> List<Map<String, T>> fetchDataFromS3(String bucketName, String path) throws IOException {
        AmazonS3 s3Client = S3ClientConfig.getInstance().getS3Client();
        if (!s3Client.doesObjectExist(bucketName, path)) {
            LOGGER.info("File {} does not exist in S3 bucket {}.", path, bucketName);
            return new ArrayList<>();
        }
        S3Object entitiesData = s3Client.getObject(new GetObjectRequest(bucketName, path));
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(entitiesData.getObjectContent()))) {
            return new ObjectMapper().configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true).readValue(reader.lines().collect(Collectors.joining("\n")), new TypeReference<List<Map<String, T>>>() {
            });
        }
    }

    public static boolean doesObjectExist(String bucketName, String path) {
        AmazonS3 s3Client = S3ClientConfig.getInstance().getS3Client();
        return s3Client.doesObjectExist(bucketName, path);
    }

    public static List<String> retrieveErrorRecords(String responseStr) {
        List<String> errorList = new ArrayList<>();
        try {
            JsonObject response = new JsonParser().parse(responseStr).getAsJsonObject();
            JsonArray items = response.getAsJsonArray("items");

            int status;
            for (JsonElement item : items) {
                JsonObject updateInfo = item.getAsJsonObject();
                status = updateInfo.getAsJsonObject("index").get("status").getAsInt();
                if (!(status == 200 || status == 201)) {
                    errorList.add(updateInfo.getAsJsonObject("index").toString());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error retrieving errror records", e);
        }

        return errorList;
    }
}
