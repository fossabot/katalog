package com.bol.katalog.security.config

import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.cqrs.read
import com.bol.katalog.security.KatalogUserDetailsHolder
import com.bol.katalog.security.Security
import com.bol.katalog.security.SecurityAggregatePermissionManager
import com.bol.katalog.utils.runBlockingAsSystem
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.session.ReactiveMapSessionRepository
import reactor.core.publisher.Mono

@Configuration
class SecurityAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun sessionRepository() = ReactiveMapSessionRepository(mutableMapOf())

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun apiSecurityWebFilterChain(
        http: ServerHttpSecurity,
        customizers: List<ServerHttpSecurityCustomizer>
    ): SecurityWebFilterChain {
        http
            .authorizeExchange()
            .anyExchange().permitAll()

        http
            .logout().logoutUrl("/api/v1/auth/logout").logoutSuccessHandler { _, _ -> Mono.empty<Void>() }

        // We don't want a HTTP basic auth popup
        http
            .httpBasic().disable()

        // No CSRF for now
        http
            .csrf().disable()

        customizers.forEach { it.customize(http) }

        return http.build()
    }

    @Bean
    fun userDetailsService(
        security: Aggregate<Security>
    ): ReactiveUserDetailsService {
        return ReactiveUserDetailsService { username ->
            @Suppress("BlockingMethodInNonBlockingContext")
            val user = runBlockingAsSystem { security.read { findUserByUsername(username) } }
                ?: return@ReactiveUserDetailsService Mono.empty()

            Mono.just(
                KatalogUserDetailsHolder(user)
            )
        }
    }

    @Bean
    fun permissionManager(security: Aggregate<Security>) = SecurityAggregatePermissionManager(security)
}