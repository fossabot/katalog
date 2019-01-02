package com.bol.katalog.config

import com.bol.katalog.config.security.ServerHttpSecurityCustomizer
import com.bol.katalog.security.tokens.TokenService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.reactor.mono
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Configuration
class ApiSecurityAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun apiSecurityWebFilterChain(
        http: ServerHttpSecurity,
        tokenService: TokenService,
        customizers: List<ServerHttpSecurityCustomizer>
    ): SecurityWebFilterChain {
        http
            .authorizeExchange()
            .anyExchange().permitAll()

        http
            .addFilterAt(bearerAuthenticationFilter(tokenService), SecurityWebFiltersOrder.AUTHENTICATION)

        http
            .logout().logoutUrl("/api/v1/auth/logout").logoutSuccessHandler { _, _ -> Mono.empty<Void>() }

        http
            .httpBasic().disable()
            .csrf().disable()

        customizers.forEach { it.customize(http) }

        return http.build()
    }

    private fun bearerAuthenticationFilter(tokenService: TokenService): AuthenticationWebFilter {
        val authManager = BearerTokenReactiveAuthenticationManager()
        val bearerAuthenticationFilter = AuthenticationWebFilter(authManager)
        val bearerConverter =
            ServerHttpBearerAuthenticationConverter(tokenService)

        bearerAuthenticationFilter.setServerAuthenticationConverter(bearerConverter)
        bearerAuthenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/api/**"))

        return bearerAuthenticationFilter
    }

    inner class BearerTokenReactiveAuthenticationManager : ReactiveAuthenticationManager {
        override fun authenticate(authentication: Authentication): Mono<Authentication> {
            return Mono.just(authentication)
        }
    }

    class ServerHttpBearerAuthenticationConverter(private val tokenService: TokenService) :
        ServerAuthenticationConverter {
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
}