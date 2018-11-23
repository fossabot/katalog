package com.bol.katalog.api;

import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

/**
 * This needs to happen in Java because of https://jira.spring.io/browse/SPR-16057
 */
public class TestHelper {
    private TestHelper() {
    }

    static WebTestClient getClient(ApplicationContext context) {
        return WebTestClient
                .bindToApplicationContext(context)
                .apply(springSecurity())
                .configureClient()
                .build();
    }
}
