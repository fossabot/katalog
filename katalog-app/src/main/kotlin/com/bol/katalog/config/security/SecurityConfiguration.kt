package com.bol.katalog.config.security

import com.bol.katalog.security.SecurityConfigurationProperties
import com.bol.katalog.security.tokens.JwtTokenService
import com.bol.katalog.security.tokens.TokenService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.session.ReactiveMapSessionRepository
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession
import org.springframework.web.server.session.HeaderWebSessionIdResolver
import org.springframework.web.server.session.WebSessionIdResolver

@Configuration
@EnableSpringWebSession
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfiguration {
    @Bean
    fun sessionRepository() = ReactiveMapSessionRepository(mutableMapOf())

    @Bean
    fun webSessionIdResolver(): WebSessionIdResolver {
        val resolver = HeaderWebSessionIdResolver()
        resolver.headerName = "X-AUTH-TOKEN"
        return resolver
    }

    @Bean
    fun tokenService(
        config: SecurityConfigurationProperties,
        userDetailsService: ReactiveUserDetailsService
    ): TokenService = JwtTokenService(config.token.hmacShaKey, userDetailsService)
}
