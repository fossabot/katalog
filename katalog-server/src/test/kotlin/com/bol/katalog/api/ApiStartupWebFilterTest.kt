package com.bol.katalog.api

import com.bol.katalog.api.support.WebClientHelper
import com.bol.katalog.config.ApiStartupWebFilter
import com.bol.katalog.config.StartupRunnerManager
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ApiStartupWebFilterTest {
    private val testStartupRunnerManager = TestStartupRunnerManager()
    private val filter = ApiStartupWebFilter(testStartupRunnerManager)
    private val client = WebClientHelper.getTestClientForFilter(filter)

    @Test
    fun `Filter passes through API calls when not starting up`() {
        testStartupRunnerManager.hasCompleted = true

        client.get().uri("/api/v1/foo")
            .exchange()
            .expectStatus().isOk
            .expectHeader().doesNotExist("X-Katalog-Starting")
    }

    @Test
    fun `Filter returns header and status code for API calls when starting up`() {
        testStartupRunnerManager.hasCompleted = false

        client.get().uri("/api/v1/foo")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectHeader().valueEquals("X-Katalog-Starting", "true")
    }

    @Test
    fun `Filter passes through non-API calls when starting up`() {
        testStartupRunnerManager.hasCompleted = false

        client.get().uri("/favicon.ico")
            .exchange()
            .expectStatus().isOk
            .expectHeader().doesNotExist("X-Katalog-Starting")
    }

    class TestStartupRunnerManager : StartupRunnerManager {
        var hasCompleted: Boolean = false
        override fun hasCompleted() = hasCompleted
    }
}