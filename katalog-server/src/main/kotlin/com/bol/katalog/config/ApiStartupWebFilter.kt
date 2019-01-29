package com.bol.katalog.config

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * If the application is still in a starting up state, we reply to all API calls with a 'starting up' response.
 * This will be used by the UI to redirect to a 'starting up' page.
 */
@Component
class ApiStartupWebFilter(val startupRunnerManager: StartupRunnerManager) : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return if (exchange.request.uri.path.toLowerCase().startsWith("/api") && !startupRunnerManager.hasCompleted()) {
            val response = exchange.response
            response.statusCode = HttpStatus.SERVICE_UNAVAILABLE
            response.headers["X-Katalog-Starting"] = "true"
            return response.writeWith(Mono.empty())
        } else {
            chain.filter(exchange)
        }
    }
}