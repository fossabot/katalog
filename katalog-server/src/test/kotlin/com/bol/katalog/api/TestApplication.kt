package com.bol.katalog.api

import com.bol.katalog.api.support.ResettableAggregate
import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.cqrs.support.TestAggregateContext
import com.bol.katalog.features.registry.Registry
import com.bol.katalog.features.registry.RegistryAggregate
import com.bol.katalog.security.*
import com.bol.katalog.security.config.SecurityConfigurationProperties
import com.bol.katalog.security.support.*
import com.bol.katalog.store.BlobStore
import com.bol.katalog.store.inmemory.InMemoryBlobStore
import com.bol.katalog.users.GroupPermission
import com.bol.katalog.utils.runBlockingAsSystem
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@SpringBootApplication
class TestApplication {
    @Bean
    fun registry(permissionManager: PermissionManager): Aggregate<Registry> {
        return ResettableAggregate {
            RegistryAggregate(
                TestAggregateContext(),
                permissionManager,
                blobStore()
            )
        }
    }

    @Bean
    fun testSecurityAggregate(): Aggregate<Security> {
        val security = SecurityAggregate(TestAggregateContext())
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
    fun apiSecurityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        // Basic Spring Security setup (if we don't provide this the default setup will simply forbid everything)
        http
            .authorizeExchange()
            .anyExchange().permitAll()

        http
            .csrf().disable()

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