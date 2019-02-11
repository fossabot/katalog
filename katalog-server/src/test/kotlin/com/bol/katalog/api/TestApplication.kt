package com.bol.katalog.api

import com.bol.katalog.cqrs.hazelcast.HazelcastAggregateContext
import com.bol.katalog.features.registry.RegistryAggregate
import com.bol.katalog.security.PermissionManager
import com.bol.katalog.security.SecurityAggregate
import com.bol.katalog.security.SecurityAggregatePermissionManager
import com.bol.katalog.security.config.SecurityConfigurationProperties
import com.bol.katalog.security.config.ServerHttpSecurityCustomizer
import com.bol.katalog.security.tokens.TokensAggregate
import com.bol.katalog.security.tokens.auth.JwtTokenService
import com.bol.katalog.security.tokens.spring.BearerSecurityCustomizer
import com.bol.katalog.store.BlobStore
import com.bol.katalog.store.inmemory.InMemoryBlobStore
import com.bol.katalog.support.TestHazelcastAggregateContext
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@SpringBootApplication
@Import(BearerSecurityCustomizer::class, JwtTokenService::class)
class TestApplication {
    @Bean
    fun aggregateContext(): TestHazelcastAggregateContext = TestHazelcastAggregateContext.get()

    @Bean
    fun registry(permissionManager: PermissionManager, aggregateContext: HazelcastAggregateContext): RegistryAggregate {
        return RegistryAggregate(
            aggregateContext,
            permissionManager,
            blobStore()
        )
    }

    @Bean
    fun testSecurityAggregate(aggregateContext: HazelcastAggregateContext): SecurityAggregate {
        return SecurityAggregate(aggregateContext)
    }

    @Bean
    fun tokens(
        aggregateContext: HazelcastAggregateContext,
        security: SecurityAggregate
    ): TokensAggregate = TokensAggregate(aggregateContext, security)

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

    @Bean
    fun permissionManager(security: SecurityAggregate): PermissionManager {
        return SecurityAggregatePermissionManager(security)
    }

    @Bean
    fun blobStore(): BlobStore = InMemoryBlobStore()

    @Bean
    fun securityConfigurationProperties(): SecurityConfigurationProperties = SecurityConfigurationProperties()
}