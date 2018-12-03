package com.bol.katalog.config.security

import com.bol.katalog.config.KatalogConfigurationProperties
import com.bol.katalog.security.tokens.TokenService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import reactor.core.publisher.Mono

@Configuration
class ApiSecurityConfiguration {
    @Bean
    fun apiSecurityWebFilterChain(
        http: ServerHttpSecurity,
        tokenService: TokenService,
        configuration: KatalogConfigurationProperties
    ): SecurityWebFilterChain = http
        .authorizeExchange()
        .anyExchange().permitAll()
        .and()
        .addFilterAt(bearerAuthenticationFilter(tokenService), SecurityWebFiltersOrder.AUTHENTICATION)
        .formLogin().securityContextRepository(WebSessionServerSecurityContextRepository()).loginPage("/api/v1/auth/login")
        .authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
        .authenticationSuccessHandler { _, _ -> Mono.empty<Void>() }
        .authenticationFailureHandler { filterExchange, _ ->
            Mono.fromRunnable {
                filterExchange.exchange.response.apply {
                    statusCode = HttpStatus.UNAUTHORIZED
                }
            }
        }
        .and()
        .logout().logoutUrl("/api/v1/auth/logout").logoutSuccessHandler { _, _ -> Mono.empty<Void>() }
        .and()
        .csrf().disable()
        .build()

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