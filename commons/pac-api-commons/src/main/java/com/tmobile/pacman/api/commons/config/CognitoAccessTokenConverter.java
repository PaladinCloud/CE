package com.tmobile.pacman.api.commons.config;

import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.util.Map;

public class CognitoAccessTokenConverter extends JwtAccessTokenConverter {
    private static final String COGNITO_GROUPS = "cognito:groups";
    private static final String SPRING_AUTHORITIES = "authorities";
    private static final String COGNITO_USERNAME = "username";
    private static final String SPRING_USER_NAME = "user_name";

    @SuppressWarnings("unchecked")
    @Override
    public OAuth2Authentication extractAuthentication(Map<String, ?> claims) {

        if (claims.containsKey(COGNITO_GROUPS))
            ((Map<String, Object>) claims).put(SPRING_AUTHORITIES, claims.get(COGNITO_GROUPS));
        if (claims.containsKey(COGNITO_USERNAME))
            ((Map<String, Object>) claims).put(SPRING_USER_NAME, claims.get(COGNITO_USERNAME));
        return super.extractAuthentication(claims);
    }
}
