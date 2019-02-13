package com.bol.katalog.api

import com.bol.katalog.config.KatalogConfigurationProperties
import com.bol.katalog.security.config.ServerHttpSecurityCustomizer
import com.bol.katalog.support.TestHazelcastAggregateContext
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@SpringBootApplication
@ComponentScan(basePackages = ["com.bol.katalog"])
@EnableConfigurationProperties(KatalogConfigurationProperties::class)
class TestApplication {
    @Bean
    fun aggregateContext(): TestHazelcastAggregateContext = TestHazelcastAggregateContext.get()

    @Bean
    fun apiSecurityWebFilterChain(
        http: ServerHttpSecurity,
        customizers: List<ServerHttpSecurityCustomizer>
    ): SecurityWebFilterChain {
        // Basic Spring Security setup (if we don't provide this the default setup will simply forbid everything)
        http
            .authorizeExchange()
            .anyExchange().permitAll()

        http
            .csrf().disable()

        customizers.forEach { it.customize(http) }

        return http.build()
    }
}