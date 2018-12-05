package com.bol.katalog.config

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
@Profile("!test") // Don't mess with redirections during unit tests, since this will just confuse matters
class UiWebFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return if (shouldRedirectToUi(exchange.request.uri.path.toLowerCase())) {
            chain.filter(exchange.mutate().request(exchange.request.mutate().path("/index.html").build()).build())
        } else chain.filter(exchange)
    }

    private fun shouldRedirectToUi(path: String) = when {
        path.startsWith("/actuator") -> false
        path.startsWith("/api") -> false
        path.startsWith("/assets") -> false
        path.endsWith(".ico") -> false
        path.endsWith(".css") -> false
        path.endsWith(".js") -> false
        else -> true
    }
}