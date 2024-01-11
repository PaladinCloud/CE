package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.pacbot.azure.inventory.vo.RegAppCertificateVH;
import com.tmobile.pacbot.azure.inventory.vo.RegAppSecretVH;
import com.tmobile.pacbot.azure.inventory.vo.RegisteredApplicationVH;
import com.tmobile.pacman.commons.azure.clients.AzureCredentialManager;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RegisteredApplicationInventoryCollector {

    // constants for API data
    private static final String VALUE = "value";
    private static final String NEXT_DATASET = "@odata.nextLink";

    // constants for the application details
    private static final String OBJECT_ID = "id";
    private static final String APP_ID = "appId";
    private static final String CREATE_DATETIME = "createdDateTime";
    private static final String DISPLAYNAME = "displayName";
    private static final String PUBLISHERDOMAIN = "publisherDomain";
    private static final String CERTIFICATE_DATA = "keyCredentials";
    private static final String SECRET_DATA = "passwordCredentials";

    // constants for secret keys
    private static final String CUSTOM_KEY_IDENTIFIER = "customKeyIdentifier";
    private static final String END_DATETIME = "endDateTime";
    private static final String START_DATETIME = "startDateTime";
    private static final String KEY_ID = "keyId";
    private static final String SECRET_TEXT = "secretText";
    private static final String HINT = "hint";

    // constants for certificate data
    private static final String TYPE = "type";
    private static final String USAGE = "usage";
    private static final String KEY = "key";

    private static final String API_URL_TEMPLATE = "https://graph.microsoft.com/beta/applications";
    private static final String TOKEN_TYPE = "Bearer";

    public List<RegisteredApplicationVH> fetchAzureRegisteredApplication() {
        List<RegisteredApplicationVH> registeredApplicationList = new ArrayList<>();
        String accessToken;
        try {
            accessToken = AzureCredentialManager.getGraphApiAuthToken();
        } catch (Exception e1) {
            return registeredApplicationList;
        }

        String url = API_URL_TEMPLATE;
        try {
            do {
                String registeredApplicationString = CommonUtils.doHttpGet(url, TOKEN_TYPE, accessToken);
                JsonObject responseObj = new JsonParser().parse(registeredApplicationString).getAsJsonObject();
                registeredApplicationList.addAll(createRegisteredApplicationInfo(responseObj.getAsJsonArray(VALUE)));
                url = responseObj.has(NEXT_DATASET) ? responseObj.get(NEXT_DATASET).getAsString() : null;
            } while (!StringUtils.isEmpty(url));
            System.out.println("Registered Application Collected " + registeredApplicationList.size());
        } catch (Exception e) {
            System.out.println("Error in collecting Registered application list");
            e.printStackTrace();
        }

        return registeredApplicationList;
    }

    /**
     * To create the list of register applications from the register application json
     *
     * @param registeredApplicationJsonArray register application json array
     * @return list of register application
     */
    private List<RegisteredApplicationVH> createRegisteredApplicationInfo(JsonArray registeredApplicationJsonArray) {

        List<RegisteredApplicationVH> registeredApplicationList = new ArrayList<>();
        for (JsonElement registeredApplicationElement : registeredApplicationJsonArray) {

            RegisteredApplicationVH registeredApplication = new RegisteredApplicationVH();
            JsonObject registeredApplicationInfo = registeredApplicationElement.getAsJsonObject();

            registeredApplication.setObjectId(getStringValueForJsonElement(registeredApplicationInfo.get(OBJECT_ID)));
            registeredApplication.setAppId(getStringValueForJsonElement(registeredApplicationInfo.get(APP_ID)));
            registeredApplication.setCreatedDateTime(getStringValueForJsonElement(registeredApplicationInfo.get(CREATE_DATETIME)));
            registeredApplication.setDisplayName(getStringValueForJsonElement(registeredApplicationInfo.get(DISPLAYNAME)));
            registeredApplication.setPublisherDomain(getStringValueForJsonElement(registeredApplicationInfo.get(PUBLISHERDOMAIN)));
            registeredApplication.setCertificateList(createRegisterApplicationCertificateList(
                    registeredApplicationInfo.getAsJsonArray(CERTIFICATE_DATA)));
            registeredApplication.setSecretList(createRegisterApplicationSecretList(
                    registeredApplicationInfo.getAsJsonArray(SECRET_DATA)));

            registeredApplicationList.add(registeredApplication);
        }

        return registeredApplicationList;
    }

    /**
     * to create the register application certificate list for an application from the certificate json
     *
     * @param certificateJsonArray certificate json array
     * @return app certificate list
     */
    private List<RegAppCertificateVH> createRegisterApplicationCertificateList(JsonArray certificateJsonArray) {
        List<RegAppCertificateVH> regAppCertificateList = new ArrayList<>();

        for (JsonElement certificateJsonElement : certificateJsonArray) {
            RegAppCertificateVH regAppCertificate = new RegAppCertificateVH();
            JsonObject regAppCertificateJsonObject = certificateJsonElement.getAsJsonObject();

            regAppCertificate.setCustomKeyIdentifier(getStringValueForJsonElement(regAppCertificateJsonObject.get(CUSTOM_KEY_IDENTIFIER)));
            regAppCertificate.setEndDateTime(getStringValueForJsonElement(regAppCertificateJsonObject.get(END_DATETIME)));
            regAppCertificate.setDisplayName(getStringValueForJsonElement(regAppCertificateJsonObject.get(DISPLAYNAME)));
            regAppCertificate.setKey(getStringValueForJsonElement(regAppCertificateJsonObject.get(KEY)));
            regAppCertificate.setStartDateTime(getStringValueForJsonElement(regAppCertificateJsonObject.get(START_DATETIME)));
            regAppCertificate.setType(getStringValueForJsonElement(regAppCertificateJsonObject.get(TYPE)));
            regAppCertificate.setUsage(getStringValueForJsonElement(regAppCertificateJsonObject.get(USAGE)));
            regAppCertificate.setKeyId(getStringValueForJsonElement(regAppCertificateJsonObject.get(KEY_ID)));

            regAppCertificateList.add(regAppCertificate);
        }

        return regAppCertificateList;
    }

    /**
     * to create the register application secret list for an application from the secret json
     *
     * @param secretJsonArray secret json array
     * @return app secret list
     */
    private List<RegAppSecretVH> createRegisterApplicationSecretList(JsonArray secretJsonArray) {
        List<RegAppSecretVH> regAppSecretList = new ArrayList<>();

        for (JsonElement secretJsonElement : secretJsonArray) {
            RegAppSecretVH regAppSecret = new RegAppSecretVH();
            JsonObject regAppSecretJsonObject = secretJsonElement.getAsJsonObject();

            regAppSecret.setCustomKeyIdentifier(getStringValueForJsonElement(regAppSecretJsonObject.get(CUSTOM_KEY_IDENTIFIER)));
            regAppSecret.setDisplayName(getStringValueForJsonElement(regAppSecretJsonObject.get(DISPLAYNAME)));
            regAppSecret.setEndDateTime(getStringValueForJsonElement(regAppSecretJsonObject.get(END_DATETIME)));
            regAppSecret.setHint(getStringValueForJsonElement(regAppSecretJsonObject.get(HINT)));
            regAppSecret.setKeyId(getStringValueForJsonElement(regAppSecretJsonObject.get(KEY_ID)));
            regAppSecret.setSecretText(getStringValueForJsonElement(regAppSecretJsonObject.get(SECRET_TEXT)));
            regAppSecret.setStartDateTime(getStringValueForJsonElement(regAppSecretJsonObject.get(START_DATETIME)));

            regAppSecretList.add(regAppSecret);
        }

        return regAppSecretList;
    }

    private String getStringValueForJsonElement(JsonElement jsonElement) {
        return jsonElement.isJsonNull() ? null : jsonElement.getAsString();
    }
}
