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
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository
import org.springframework.session.ReactiveMapSessionRepository
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession
import org.springframework.web.server.session.HeaderWebSessionIdResolver
import org.springframework.web.server.session.WebSessionIdResolver
import reactor.core.publisher.Mono


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
    @EnableSpringWebSession
    @EnableWebFluxSecurity
    class SecurityFallbackConfiguration {
        @Bean
        fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain = http
                .authorizeExchange()
                .pathMatchers("/api/**").hasAuthority("ROLE_USER")
                .anyExchange().permitAll()
                .and()
                .formLogin().securityContextRepository(WebSessionServerSecurityContextRepository()).loginPage("/api/v1/auth/login")
                .authenticationSuccessHandler { _, _ -> Mono.empty<Void>() }
                .authenticationFailureHandler { filterExchange, _ ->
                    Mono.fromRunnable {
                        filterExchange.exchange.response.apply {
                            statusCode = HttpStatus.UNAUTHORIZED
                        }
                    }
                }
                .and()
                .httpBasic()
                .and()
                .csrf().disable()
                .build()

        @Bean
        fun sessionRepository() = ReactiveMapSessionRepository(mutableMapOf())

        @Bean
        fun webSessionIdResolver(): WebSessionIdResolver {
            val resolver = HeaderWebSessionIdResolver()
            resolver.headerName = "X-AUTH-TOKEN"
            return resolver
        }

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