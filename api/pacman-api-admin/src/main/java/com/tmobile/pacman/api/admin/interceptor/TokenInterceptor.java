package com.tmobile.pacman.api.admin.interceptor;

import com.tmobile.pacman.api.commons.utils.ThreadLocalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

@Component
public class TokenInterceptor implements HandlerInterceptor {
    Logger logger = LoggerFactory.getLogger(TokenInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            // Extract the token value and set it to the cache here
            Principal principal = request.getUserPrincipal();
            String token = ((OAuth2AuthenticationDetails) ((OAuth2Authentication) principal).getDetails()).getTokenValue();
            // Set to cache
            ThreadLocalUtil.accessToken.set(token);
        } catch (Exception e) {
            logger.error("Unable to set access token", e);
        }
        return true;
    }
}
