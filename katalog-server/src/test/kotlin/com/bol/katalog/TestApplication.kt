package com.bol.katalog

import com.bol.katalog.config.KatalogAutoConfiguration
import com.bol.katalog.domain.DomainProcessor
import com.bol.katalog.security.ReactiveKatalogUserDetailsService
import com.bol.katalog.store.InMemoryBlobStore
import com.bol.katalog.store.InMemoryEventStore
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@SpringBootApplication
@ImportAutoConfiguration(KatalogAutoConfiguration::class)
@Import(DomainProcessor::class)
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
    fun passwordEncoder(): PasswordEncoder = NoOpPasswordEncoder.getInstance()

    @Bean
    fun userDetailsService(passwordEncoder: PasswordEncoder): ReactiveUserDetailsService {
        return ReactiveKatalogUserDetailsService(
            listOf(
                TestUsers.user1(),
                TestUsers.user2(),
                TestUsers.admin(),
                TestUsers.noGroupsUser()
            )
        )
    }
}