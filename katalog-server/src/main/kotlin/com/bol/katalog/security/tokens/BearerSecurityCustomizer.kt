package com.bol.katalog.security.tokens

import com.bol.katalog.security.config.ServerHttpSecurityCustomizer
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class BearerSecurityCustomizer(private val tokenService: TokenService) : ServerHttpSecurityCustomizer {
    override fun customize(http: ServerHttpSecurity) {
        http.addFilterAt(bearerAuthenticationFilter(tokenService), SecurityWebFiltersOrder.AUTHENTICATION)
    }

    private fun bearerAuthenticationFilter(tokenService: TokenService): AuthenticationWebFilter {
        val authManager = BearerTokenReactiveAuthenticationManager()
        val bearerAuthenticationFilter = AuthenticationWebFilter(authManager)
        val bearerConverter = ServerHttpBearerAuthenticationConverter(tokenService)

        bearerAuthenticationFilter.setServerAuthenticationConverter(bearerConverter)
        bearerAuthenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/api/**"))

        return bearerAuthenticationFilter
    }

    inner class BearerTokenReactiveAuthenticationManager : ReactiveAuthenticationManager {
        override fun authenticate(authentication: Authentication): Mono<Authentication> {
            return Mono.just(authentication)
        }
    }
}