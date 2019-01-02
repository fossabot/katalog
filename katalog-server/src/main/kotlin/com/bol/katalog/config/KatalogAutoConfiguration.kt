package com.bol.katalog.config

import com.bol.katalog.config.security.SecurityConfigurationProperties
import com.bol.katalog.security.tokens.JwtTokenService
import com.bol.katalog.security.tokens.TokenService
import com.bol.katalog.store.BlobStore
import com.bol.katalog.store.EventStore
import com.bol.katalog.store.InMemoryBlobStore
import com.bol.katalog.store.InMemoryEventStore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.session.ReactiveMapSessionRepository
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import java.time.Clock

@Configuration
@EnableSpringWebSession
@EnableWebFlux
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class KatalogAutoConfiguration : WebFluxConfigurer {
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
    fun tokenService(
        config: SecurityConfigurationProperties,
        userDetailsService: ReactiveUserDetailsService
    ): TokenService = JwtTokenService(config.token.hmacShaKey, userDetailsService)
}