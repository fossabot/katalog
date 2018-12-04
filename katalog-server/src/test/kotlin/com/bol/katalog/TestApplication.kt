package com.bol.katalog

import com.bol.katalog.domain.DomainProcessor
import com.bol.katalog.domain.Group
import com.bol.katalog.security.*
import com.bol.katalog.security.tokens.TokenService
import com.bol.katalog.store.InMemoryBlobStore
import com.bol.katalog.store.InMemoryEventStore
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@SpringBootApplication
@Import(DomainProcessor::class)
@EnableWebFluxSecurity
class TestApplication {
    @Bean
    fun eventStore() = InMemoryEventStore()

    @Bean
    fun blobStore() = InMemoryBlobStore()

    @Bean
    fun userDetailsProvider(): CurrentUserSupplier =
        ReactiveSecurityContextCurrentUserSupplier()

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
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun userDetailsService(): ReactiveUserDetailsService {
        val user = KatalogUserDetailsHolder(
            "user",
            passwordEncoder().encode("user"),
            listOf(SimpleGrantedAuthority("ROLE_USER")),
            emptyList()
        )

        val admin = KatalogUserDetailsHolder(
            "admin",
            passwordEncoder().encode("admin"),
            listOf(SimpleGrantedAuthority("ROLE_USER"), SimpleGrantedAuthority("ROLE_ADMIN")),
            emptyList()
        )

        return ReactiveKatalogUserDetailsService(listOf(user, admin))
    }

    @Bean
    fun tokenService(userDetailsService: ReactiveUserDetailsService): TokenService = object : TokenService {
        override suspend fun authenticate(token: String): Authentication? {
            val user = userDetailsService.findByUsername(token).awaitSingle()

            return UsernamePasswordAuthenticationToken(
                user,
                user.password
            )
        }

        override suspend fun issueToken(
            user: KatalogUserDetails,
            authorities: Collection<GrantedAuthority>,
            groups: Collection<Group>
        ): String {
            throw NotImplementedError()
        }
    }
}