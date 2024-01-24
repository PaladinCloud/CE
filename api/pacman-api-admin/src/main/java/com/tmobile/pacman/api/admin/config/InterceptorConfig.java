package com.tmobile.pacman.api.admin.config;

import com.tmobile.pacman.api.admin.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor).addPathPatterns("/accounts", "/accounts/*/create", "/accounts/*/delete",
                "/policy/enable-disable", "/asset-group-exception/configure", "/asset-group-exception/delete");
    }
}
