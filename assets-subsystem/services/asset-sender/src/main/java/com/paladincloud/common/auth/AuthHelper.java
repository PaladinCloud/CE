package com.paladincloud.common.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paladincloud.common.config.ConfigConstants.PaladinCloud;
import com.paladincloud.common.config.ConfigService;
import com.paladincloud.common.errors.JobException;
import com.paladincloud.common.util.HttpHelper;
import com.paladincloud.common.util.HttpHelper.AuthorizationType;
import com.paladincloud.common.util.JsonHelper;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Singleton
public class AuthHelper {

    private static final Logger LOGGER = LogManager.getLogger(AuthHelper.class);
    private static final String API_READ_SCOPE = "API_OPERATION/READ";
    private AccessToken accessToken;

    @Inject
    public AuthHelper() {
    }

    public String getToken() throws IOException {
        if (!isTokenValid()) {
            authorize();
        }
        return accessToken.getToken();
    }

    private synchronized void authorize() throws IOException {
        if (isTokenValid()) {
            return;
        }
        LOGGER.info("authorizing access token");
        var url = STR."\{ConfigService.get(PaladinCloud.AUTH_API_URL)}/oauth2/token?grant_type=client_credentials&scope=\{API_READ_SCOPE}";
        var response = HttpHelper.post(url, "", AuthorizationType.BASIC, ConfigService.get(PaladinCloud.API_AUTH_CREDENTIALS));
        var authInfo = JsonHelper.fromString(AccessToken.class, response);
        if (authInfo.getToken() == null) {
            throw new JobException("Failed getting access token");
        }
        authInfo.initExpireTime();
        accessToken = authInfo;
    }

    private boolean isTokenValid() {
        return accessToken != null && accessToken.getExpiresAt() > System.currentTimeMillis();
    }
}

class AccessToken {

    @Getter
    @JsonProperty("access_token")
    private String token;
    @JsonProperty("expires_in")
    private int expiresInSeconds;

    @Getter
    private long expiresAt = 0;

    public void initExpireTime() {
        // Give a short time buffer before the expiration
        expiresAt = System.currentTimeMillis() + (expiresInSeconds * 1000L) - (20L * 1000L);
    }

    public String toString() {
        return STR."Token:\{token}, expires in \{(expiresAt - System.currentTimeMillis())
            / 1000} sec";
    }
}
