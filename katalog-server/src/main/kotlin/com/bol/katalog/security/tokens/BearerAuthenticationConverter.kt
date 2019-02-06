package com.bol.katalog.security.tokens

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpHeaders
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class ServerHttpBearerAuthenticationConverter(private val tokenService: TokenService) : ServerAuthenticationConverter {
    override fun convert(serverWebExchange: ServerWebExchange): Mono<Authentication> = GlobalScope.mono {
        val authHeader = serverWebExchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        if (authHeader != null && authHeader.length > BEARER.length) {
            val bearer = authHeader.substring(BEARER.length)
            tokenService.authenticate(bearer)
        } else {
            null
        }
    }

    companion object {
        private const val BEARER = "Bearer "
    }
}