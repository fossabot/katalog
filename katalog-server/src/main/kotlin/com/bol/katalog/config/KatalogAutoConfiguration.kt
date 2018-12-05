package com.bol.katalog.config

import com.bol.katalog.security.SecurityConfigurationProperties
import com.bol.katalog.security.tokens.JwtTokenService
import com.bol.katalog.security.tokens.TokenService
import com.bol.katalog.store.BlobStore
import com.bol.katalog.store.EventStore
import com.bol.katalog.store.InMemoryBlobStore
import com.bol.katalog.store.InMemoryEventStore
import com.sun.org.apache.xerces.internal.parsers.SecurityConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.session.ReactiveMapSessionRepository
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession
import org.springframework.web.server.session.HeaderWebSessionIdResolver
import org.springframework.web.server.session.WebSessionIdResolver
import java.time.Clock

@Configuration
@EnableSpringWebSession
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class KatalogAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun eventStore(): EventStore = InMemoryEventStore()

    @Bean
    @ConditionalOnMissingBean
    fun blobStore(): BlobStore = InMemoryBlobStore()

    @Bean
    @ConditionalOnMissingBean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    @ConditionalOnMissingBean
    fun sessionRepository() = ReactiveMapSessionRepository(mutableMapOf())

    @Bean
    @ConditionalOnMissingBean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    @ConditionalOnMissingBean
    fun webSessionIdResolver(): WebSessionIdResolver {
        val resolver = HeaderWebSessionIdResolver()
        resolver.headerName = "X-AUTH-TOKEN"
        return resolver
    }

    @Bean
    @ConditionalOnMissingBean
    fun tokenService(
        config: SecurityConfigurationProperties,
        userDetailsService: ReactiveUserDetailsService
    ): TokenService = JwtTokenService(config.token.hmacShaKey, userDetailsService)
}