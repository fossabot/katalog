package com.bol.katalog.api

import com.bol.katalog.api.support.ResettableAggregate
import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.send
import com.bol.katalog.cqrs.support.TestAggregateContext
import com.bol.katalog.features.registry.Registry
import com.bol.katalog.features.registry.RegistryAggregate
import com.bol.katalog.security.*
import com.bol.katalog.security.config.SecurityConfigurationProperties
import com.bol.katalog.security.config.ServerHttpSecurityCustomizer
import com.bol.katalog.security.support.*
import com.bol.katalog.security.tokens.Tokens
import com.bol.katalog.security.tokens.TokensAggregate
import com.bol.katalog.security.tokens.auth.JwtTokenService
import com.bol.katalog.security.tokens.spring.BearerSecurityCustomizer
import com.bol.katalog.store.BlobStore
import com.bol.katalog.store.inmemory.InMemoryBlobStore
import com.bol.katalog.users.GroupPermission
import com.bol.katalog.utils.runBlockingAsSystem
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@SpringBootApplication
@Import(BearerSecurityCustomizer::class, JwtTokenService::class)
class TestApplication {
    @Bean
    fun aggregateContext(): AggregateContext = TestAggregateContext()

    @Bean
    fun registry(permissionManager: PermissionManager, aggregateContext: AggregateContext): Aggregate<Registry> {
        return ResettableAggregate {
            RegistryAggregate(
                aggregateContext,
                permissionManager,
                blobStore()
            )
        }
    }

    @Bean
    fun testSecurityAggregate(aggregateContext: AggregateContext): Aggregate<Security> {
        val security = SecurityAggregate(aggregateContext)
        runBlockingAsSystem {
            security.send(group1.create())

            security.send(user1.create())
            security.send(userReadOnly.create())
            security.send(admin.create())
            security.send(userNoGroups.create())

            security.send(user1.addToGroup(group1, allPermissions()))
            security.send(userReadOnly.addToGroup(group1, setOf(GroupPermission.READ)))
        }
        return security
    }

    @Bean
    fun tokens(
        aggregateContext: AggregateContext,
        security: Aggregate<Security>
    ): Aggregate<Tokens> = ResettableAggregate { TokensAggregate(aggregateContext, security) }

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
    fun permissionManager(security: Aggregate<Security>): PermissionManager {
        return SecurityAggregatePermissionManager(security)
    }

    @Bean
    fun blobStore(): BlobStore = InMemoryBlobStore()

    @Bean
    fun securityConfigurationProperties(): SecurityConfigurationProperties = SecurityConfigurationProperties()
}