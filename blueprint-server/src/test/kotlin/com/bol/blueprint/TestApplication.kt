package com.bol.blueprint

import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.domain.Group
import com.bol.blueprint.domain.UserGroupService
import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.InMemoryEventStore
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Flux

@SpringBootApplication
@Import(CommandHandler::class)
@EnableWebFluxSecurity
class TestApplication {
    @Bean
    fun eventStore() = InMemoryEventStore()

    @Bean
    fun blobStore() = InMemoryBlobStore()

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

    @Bean
    fun userGroupService(): UserGroupService {
        return object : UserGroupService {
            override fun getGroupsByUsername(username: String): Flux<Group> = Flux.empty()
        }
    }
}