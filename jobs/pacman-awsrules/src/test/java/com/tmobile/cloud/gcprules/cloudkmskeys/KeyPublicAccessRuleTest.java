package com.tmobile.cloud.gcprules.cloudkmskeys;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.gcprules.cloudstorage.CloudStorageWithPublicAccessRule;
import com.tmobile.cloud.gcprules.utils.GCPUtils;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.policy.Annotation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static com.tmobile.cloud.gcprules.utils.TestUtils.getHitsJsonArrayForCloudStoragePublicAccessFailure;
import static com.tmobile.cloud.gcprules.utils.TestUtils.getHitsJsonArrayForCloudStoragePublicAccessSuccess;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, GCPUtils.class, Annotation.class})
public class KeyPublicAccessRuleTest {
    @InjectMocks
    KeyPublicAccessRule keyPublicAccessRule;

    @Before
    public void setUp() {
        mockStatic(PacmanUtils.class);
        mockStatic(GCPUtils.class);
    }

    @Test
    public void executeSuccessTest() throws Exception {
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitsJsonForSuccess());
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(keyPublicAccessRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_SUCCESS));

    }

    @Test
    public void executeFailureTest() throws Exception {
        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitsJsonForFailure());
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
        assertThat(keyPublicAccessRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void executeFailureTestWithInvalidInputException() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitsJsonForFailure());
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                false);
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
        assertThatThrownBy(() -> keyPublicAccessRule.execute(getMapString("r_123 "), getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
    }

    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "high");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "kms_key_should_not_be_public");
        commonMap.put("policyId", "kms_key_should_not_be_public");
        commonMap.put("policyVersion", "version-1");
        return commonMap;
    }

    private JsonArray getHitsJsonForSuccess() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\"_cloudType\":\"gcp\",\"region\":\"us\",\"id\":\"projects\\/cool-bay-349411\\/locations\\/us\\/keyRings\\/cool-bay-349411-key-ring\\/cryptoKeys\\/cool-bay-bigquery-cmk\",\"projectName\":\"cool-bay-349411\",\"keyRingName\":\"cool-bay-349411-key-ring\",\"name\":\"projects\\/cool-bay-349411\\/locations\\/us\\/keyRings\\/cool-bay-349411-key-ring\\/cryptoKeys\\/cool-bay-bigquery-cmk\",\"cryptoBackend\":\"\",\"purpose\":\"ENCRYPT_DECRYPT\",\"importOnly\":false,\"labelsCount\":0,\"labels\":{},\"bindings\":[{\"role\":\"roles\\/cloudkms.cryptoKeyEncrypterDecrypter\",\"members\":[\"serviceAccount:service-47822473470@gs-project-accounts.iam.gserviceaccount.com\"]}],\"discoverydate\":\"2022-07-14 11:00:00+0000\",\"_resourceid\":\"projects\\/cool-bay-349411\\/locations\\/us\\/keyRings\\/cool-bay-349411-key-ring\\/cryptoKeys\\/cool-bay-bigquery-cmk\",\"_docid\":\"projects\\/cool-bay-349411\\/locations\\/us\\/keyRings\\/cool-bay-349411-key-ring\\/cryptoKeys\\/cool-bay-bigquery-cmk\",\"_entity\":\"true\",\"_entitytype\":\"kmskey\",\"firstdiscoveredon\":\"2022-07-14 11:00:00+0000\",\"latest\":true,\"_loaddate\":\"2022-07-14 11:17:00+0000\"}", JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    private JsonArray getHitsJsonForFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\"_cloudType\":\"gcp\",\"region\":\"global\",\"id\":\"projects\\/cool-bay-349411\\/locations\\/global\\/keyRings\\/demo-key-ring1\\/cryptoKeys\\/demo-key1\",\"projectName\":\"cool-bay-349411\",\"keyRingName\":\"demo-key-ring1\",\"name\":\"projects\\/cool-bay-349411\\/locations\\/global\\/keyRings\\/demo-key-ring1\\/cryptoKeys\\/demo-key1\",\"cryptoBackend\":\"\",\"purpose\":\"ENCRYPT_DECRYPT\",\"importOnly\":false,\"labelsCount\":0,\"labels\":{},\"bindings\":[{\"role\":\"roles\\/cloudkms.cryptoKeyEncrypterDecrypter\",\"members\":[\"allUsers\",\"serviceAccount:service-47822473470@gcp-sa-pubsub.iam.gserviceaccount.com\"]}],\"discoverydate\":\"2022-07-14 11:00:00+0000\",\"_resourceid\":\"projects\\/cool-bay-349411\\/locations\\/global\\/keyRings\\/demo-key-ring1\\/cryptoKeys\\/demo-key1\",\"_docid\":\"projects\\/cool-bay-349411\\/locations\\/global\\/keyRings\\/demo-key-ring1\\/cryptoKeys\\/demo-key1\",\"_entity\":\"true\",\"_entitytype\":\"kmskey\",\"firstdiscoveredon\":\"2022-07-14 11:00:00+0000\",\"latest\":true,\"_loaddate\":\"2022-07-14 11:17:00+0000\"}", JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    private Annotation getMockAnnotation() {
        Annotation annotation=new Annotation();
        annotation.put(PacmanSdkConstants.POLICY_NAME,"Mock policy name");
        annotation.put(PacmanSdkConstants.POLICY_ID, "Mock policy id");
        annotation.put(PacmanSdkConstants.POLICY_VERSION, "Mock policy version");
        annotation.put(PacmanSdkConstants.RESOURCE_ID, "Mock resource id");
        annotation.put(PacmanSdkConstants.TYPE, "Mock type");
        return annotation;
    }

    @Test
    public void getHelpTextTest() {
        assertThat(keyPublicAccessRule.getHelpText(), is(notNullValue()));
    }
}
