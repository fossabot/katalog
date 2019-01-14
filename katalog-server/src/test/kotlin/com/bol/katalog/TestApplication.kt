package com.bol.katalog

import com.bol.katalog.config.KatalogAutoConfiguration
import com.bol.katalog.cqrs.CommandProcessor
import com.bol.katalog.security.KatalogUserDetailsHolder
import com.bol.katalog.config.inmemory.InMemoryBlobStore
import com.bol.katalog.config.inmemory.InMemoryEventStore
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono

@SpringBootApplication
@ImportAutoConfiguration(KatalogAutoConfiguration::class)
@Import(CommandProcessor::class)
class TestApplication {
    @Bean
    fun eventStore() = InMemoryEventStore()

    @Bean
    fun blobStore() = InMemoryBlobStore()

    @Bean
    fun clock() = TestData.clock

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain = http
        .csrf().disable()
        .authorizeExchange()
        .pathMatchers("/api/**").hasAuthority("ROLE_USER")
        .anyExchange().permitAll()
        .and()
        .httpBasic()
        .and()
        .build()

    @Bean
    fun userDetailsService(): ReactiveUserDetailsService {
        return ReactiveUserDetailsService { username ->
            val user = TestUsers.allUsers().singleOrNull { it.username == username }
                ?: return@ReactiveUserDetailsService Mono.empty()
            Mono.just(KatalogUserDetailsHolder(user))
        }
    }
}