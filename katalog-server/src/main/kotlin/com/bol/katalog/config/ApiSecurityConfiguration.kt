package com.bol.katalog.config

import com.bol.katalog.config.security.ServerHttpSecurityCustomizer
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono

@Configuration
class ApiSecurityAutoConfiguration {
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
}