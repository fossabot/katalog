package com.bol.katalog.api;

import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

/**
 * This needs to happen in Java because of https://jira.spring.io/browse/SPR-16057
 */
public class WebClientHelper {
    private WebClientHelper() {
    }

    public static WebTestClient getTestClientForFilter(WebFilter filter) {
        return WebTestClient
                .bindToWebHandler(exchange -> Mono.empty())
                .webFilter(filter)
                .build();
    }
}
