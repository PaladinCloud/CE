/*******************************************************************************
 * Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package com.tmobile.pacman.executor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.common.exception.ServerlessPolicyFailedException;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.PolicyResult;

// TODO: Auto-generated Javadoc
/**
 * The Class ServerlessRuleHandler.
 *
 * @author kkumar
 */
public class ServerlessPolicyHandler implements PolicyHandler {

    /** The policy params. */
    Map<String, String> policyParams;

    /** The resource. */
    Map<String, String> resource;

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(ServerlessPolicyHandler.class);

    /** The http client. */
    HttpClient httpClient;
    
    private static final String RESPONSE_STATUS_FAILED = "failed";
    public static final String SUCCESS_MESSAGE = "Policy evaluation sucessfull";
    public static final String SEVERITY = "severity";
    public static final String CATEGORY = "policyCategory";
    public static final String FAILURE_MESSAGE = "Error in policy evaluation";

    /**
     * Instantiates a new serverless policy handler.
     *
     * @param httpClient the http client
     */
    public ServerlessPolicyHandler(HttpClient httpClient) {
        super();
        this.httpClient = httpClient;
    }

    /* (non-Javadoc)
     * @see com.tmobile.pacman.executor.RuleHandler#handleRule(java.util.Map, java.util.Map)
     */
    @Override
    public PolicyResult handlePolicy(Map<String, String> policyParams, Map<String, String> resource) {

        Gson gson = new GsonBuilder().create();
        // get rule URI
        String policyUri = policyParams.get(PacmanSdkConstants.POLICY_URL_KEY);
        // do a http post of policyParams to the policyUri and try to build a
        // PolicyResult object based on return value
        // like status 200 will be a pass
        Map<String, Map<String, String>> input = new HashMap<>();
        input.put("policyParam".intern(), policyParams);
        input.put("resource".intern(), resource);
        StringBuilder requestBody = new StringBuilder(gson.toJson(input));
        Map<String, String> headers = new HashMap<String, String>();
        if (policyParams.containsKey(PacmanSdkConstants.X_API_KEY)) {
            headers.put(PacmanSdkConstants.X_API_KEY, policyParams.get(PacmanSdkConstants.X_API_KEY));
        }
        try {
            String response = doHttpPost(policyUri, requestBody.toString(), headers);
            PolicyResult result = null;
            if(response != null && !"".equals(response)) {
            	Map<String, String> responseMap = gson.fromJson(response, Map.class);
            	 if(responseMap.containsKey(PacmanSdkConstants.STATUS_KEY) && 
            			 PacmanSdkConstants.STATUS_SUCCESS.equals(responseMap.get(PacmanSdkConstants.STATUS_KEY))){
            		 result = new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, SUCCESS_MESSAGE);
            		 result.setResource(resource);
                     
            	 } else  if(responseMap.containsKey(PacmanSdkConstants.STATUS_KEY) && 
            			 RESPONSE_STATUS_FAILED.equals(responseMap.get(PacmanSdkConstants.STATUS_KEY))){
            		 Annotation annotation = Annotation.buildAnnotation(policyParams, Annotation.Type.ISSUE);
            		 annotation.put(PacmanSdkConstants.DESCRIPTION, responseMap.get(PacmanSdkConstants.DESCRIPTION));
 					annotation.put(SEVERITY, policyParams.get(SEVERITY));
 					annotation.put(CATEGORY, policyParams.get(CATEGORY));
 					 result = new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, FAILURE_MESSAGE,
 		                    annotation);
 					result.setResource(resource);
 		            
            	 } else {
            		 Annotation annotation = Annotation.buildAnnotation(policyParams, Annotation.Type.ISSUE);
 					annotation.put(SEVERITY, policyParams.get(SEVERITY));
 					annotation.put(CATEGORY, policyParams.get(CATEGORY));
 					 result = new PolicyResult(PacmanSdkConstants.STATUS_UNKNOWN, PacmanSdkConstants.STATUS_UNKNOWN_MESSAGE,
 		                    annotation);
 					result.setResource(resource);
            	 }
            			 
            }
            
            // overwrite the resource as sent in
                                         // case it was overwritten
            return result;
        }  catch (Exception e) {
            Annotation annotation = Annotation.buildAnnotation(policyParams, Annotation.Type.ISSUE);
            annotation.put("reason".intern(), "policy was unable to evaluvate");
            annotation.put("errorMessage".intern(), e.getMessage());
            logger.error("unable to execute serverless policy".intern(), e);
            return new PolicyResult(PacmanSdkConstants.STATUS_UNKNOWN, "unable to evaluvate compliance".intern(),
                    annotation);
        }
    }

    /**
     * Do http post.
     *
     * @param url the url
     * @param requestBody the request body
     * @param headers the headers
     * @return the integer
     * @throws ServerlessPolicyFailedException the serverless rule failed exception
     */
    private String doHttpPost(String url, String requestBody, Map<String, String> headers) {

        PostMethod httppost = null;
        Gson gson = new GsonBuilder().create();
        try {

            httppost = new PostMethod(url);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httppost.addRequestHeader(entry.getKey(), entry.getValue());
            }
            httppost.setRequestHeader("Content-Type", "application/json");
            httppost.setRequestEntity(new StringRequestEntity(requestBody, null, null));
            int responsecode = httpClient.executeMethod(httppost);

            // ******remove this block of code after sererless issue is solved:
            // issue about not able to send the appropriate http response code
            // Map<String,String> resp=null;
            // try{
            // resp =
            // gson.fromJson(httppost.getResponseBodyAsString(),HashMap.class);
            // }catch(Exception e){
            // logger.error("unexpected response");
            // throw new ServerlessRuleFailedException("{\"unexpected
            // response\"}",-1);
            // }
            // if(resp!=null && !resp.isEmpty()){
            // throw new
            // ServerlessRuleFailedException(httppost.getResponseBodyAsString(),responsecode);
            // }
            // ******remove this block of code after sererless issue is solved:
            // issue about not able to send the appropriate http response code

            if (HttpStatus.SC_OK == responsecode) {
                return httppost.getResponseBodyAsString();
            } 
        } catch (org.apache.http.ParseException parseException) {
            logger.error("ParseException : " + parseException.getMessage());
        } catch (IOException ioException) {
            logger.error("IOException : " + ioException.getMessage());
        } finally {
            if(null!=httppost){
                httppost.releaseConnection();
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.tmobile.pacman.executor.RuleHandler#handleRule()
     */
    @Override
    public PolicyResult handlePolicy() {
        // get rule URI
        String ruleUri = getRuleParams().get(PacmanSdkConstants.POLICY_URL_KEY);
        // do a http post of RuleParams to the ruleUri and try to build a
        // RuleResult object based on return value
        // like status 200 will be a pass
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, "this check is just passed");
    }

    /**
     * Gets the rule params.
     *
     * @return the rule params
     */
    public Map<String, String> getRuleParams() {
        return policyParams;
    }

    /**
     * Sets the rule params.
     *
     * @param ruleParams the rule params
     */
    public void setRuleParams(Map<String, String> ruleParams) {
        this.policyParams = ruleParams;
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public PolicyResult call() throws Exception {
        return handlePolicy(policyParams, resource);
    }

    /**
     * Instantiates a new serverless rule handler.
     */
    public ServerlessPolicyHandler() {

        // httpClient = new HttpClient(new
        // MultiThreadedHttpConnectionManager());
    }

    /**
     * Instantiates a new serverless rule handler.
     *
     * @param httpClient the http client
     * @param ruleParams the rule params
     * @param resource the resource
     */
    public ServerlessPolicyHandler(HttpClient httpClient, Map<String, String> ruleParams, Map<String, String> resource) {
        super();
        this.httpClient = httpClient;
        this.policyParams = ruleParams;
        this.resource = resource;
    }

    /**
     * Gets the http client.
     *
     * @return the http client
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Sets the http client.
     *
     * @param httpClient the new http client
     */
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

}
