package com.bol.blueprint

import com.bol.blueprint.config.fallback
import com.bol.blueprint.store.BlobStore
import com.bol.blueprint.store.EventStore
import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.InMemoryEventStore
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableConfigurationProperties(BlueprintConfigurationProperties::class)
class Config {
    @Bean
    @Primary
    fun eventStoreFactory(beanFactory: ListableBeanFactory): FactoryBean<EventStore> = fallback(beanFactory) { InMemoryEventStore() }

    @Bean
    @Primary
    fun blobStoreFactory(beanFactory: ListableBeanFactory): FactoryBean<BlobStore> = fallback(beanFactory) { InMemoryBlobStore() }

    @Configuration
    @ConditionalOnProperty("blueprint.security.simple.enabled", matchIfMissing = false)
    @EnableWebFluxSecurity
    class SecurityFallbackConfiguration {
        @Bean
        fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain = http
            .authorizeExchange()
            .pathMatchers("/api/**").hasAuthority("ROLE_USER")
            .anyExchange().permitAll()
            .and()
            .httpBasic()
            .and()
            .build()

        @Bean
        fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

        @Bean
        fun userDetailsService(): ReactiveUserDetailsService {
            val user = User.withUsername("user")
                .password(passwordEncoder().encode("user"))
                .roles("USER")
                .build()

            val admin = User.withUsername("admin")
                .password(passwordEncoder().encode("admin"))
                .roles("USER", "ADMIN")
                .build()

            return MapReactiveUserDetailsService(user, admin)
        }
    }
}