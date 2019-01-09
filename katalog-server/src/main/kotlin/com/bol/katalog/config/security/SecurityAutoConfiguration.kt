package com.bol.katalog.config.security

import com.bol.katalog.security.KatalogUserDetailsHolder
import com.bol.katalog.security.SecurityAggregate
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono

@Configuration
@ImportAutoConfiguration(SimpleAutoConfiguration::class)
class SecurityAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
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
    @ConditionalOnMissingBean
    fun userDetailsService(
        security: SecurityAggregate
    ): ReactiveUserDetailsService {
        return ReactiveUserDetailsService { username ->
            val user = security.findUserByUsername(username) ?: return@ReactiveUserDetailsService Mono.empty()

            Mono.just(
                KatalogUserDetailsHolder(user)
            )
        }
    }

}