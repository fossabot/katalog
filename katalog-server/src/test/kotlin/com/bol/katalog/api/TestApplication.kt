package com.bol.katalog.api

import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.cqrs.TestAggregateContext
import com.bol.katalog.features.registry.RegistryAggregate
import com.bol.katalog.features.registry.RegistryState
import com.bol.katalog.security.*
import com.bol.katalog.store.inmemory.InMemoryBlobStore
import kotlinx.coroutines.runBlocking
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@SpringBootApplication
class TestApplication {
    @Bean
    fun registry(permissionManager: PermissionManager): Aggregate<RegistryState> {
        return ResettableAggregate { RegistryAggregate(TestAggregateContext(), permissionManager, InMemoryBlobStore()) }
    }

    @Bean
    fun testSecurityAggregate(): Aggregate<SecurityState> {
        val security = SecurityAggregate(TestAggregateContext())
        runBlocking {
            security.send(group1.create())

            security.send(user1.create())
            security.send(admin.create())
            security.send(userNoGroups.create())

            security.send(user1.addToGroup(group1, allPermissions()))
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
    fun permissionManager(security: Aggregate<SecurityState>): PermissionManager {
        return ReactivePermissionManager(security)
    }
}