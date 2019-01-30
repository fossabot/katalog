package com.bol.katalog.security.config

import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.security.CoroutineContextPermissionManager
import com.bol.katalog.security.KatalogUserDetailsHolder
import com.bol.katalog.security.SecurityState
import kotlinx.coroutines.runBlocking
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

        http
            .httpBasic().disable()
            .csrf().disable()

        customizers.forEach { it.customize(http) }

        return http.build()
    }

    @Bean
    fun userDetailsService(
        security: Aggregate<SecurityState>
    ): ReactiveUserDetailsService {
        return ReactiveUserDetailsService { username ->
            @Suppress("BlockingMethodInNonBlockingContext")
            val user = runBlocking { security.read { findUserByUsername(username) } }
                ?: return@ReactiveUserDetailsService Mono.empty()

            Mono.just(
                KatalogUserDetailsHolder(user)
            )
        }
    }

    @Bean
    fun permissionManager(security: Aggregate<SecurityState>) = CoroutineContextPermissionManager(security)
}