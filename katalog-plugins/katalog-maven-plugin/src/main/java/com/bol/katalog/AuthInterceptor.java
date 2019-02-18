package com.bol.katalog;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class AuthInterceptor implements RequestInterceptor {
    private final String headerValue;

    AuthInterceptor(String token) {
        this.headerValue = "Bearer " + token;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header("Authorization", headerValue);
    }
}
