package com.bol.katalog

import com.bol.katalog.config.KatalogAutoConfiguration
import com.bol.katalog.domain.DomainProcessor
import com.bol.katalog.domain.Group
import com.bol.katalog.security.KatalogUserDetails
import com.bol.katalog.security.KatalogUserDetailsHolder
import com.bol.katalog.security.ReactiveKatalogUserDetailsService
import com.bol.katalog.security.groups.GroupService
import com.bol.katalog.security.tokens.TokenService
import com.bol.katalog.store.InMemoryBlobStore
import com.bol.katalog.store.InMemoryEventStore
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
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
    fun userDetailsService(passwordEncoder: PasswordEncoder): ReactiveUserDetailsService {
        val user = KatalogUserDetailsHolder(
            "user",
            passwordEncoder.encode("user"),
            listOf(SimpleGrantedAuthority("ROLE_USER")),
            listOf(Group("group1"), Group("group2"))
        )

        val user2 = KatalogUserDetailsHolder(
            "user2",
            passwordEncoder.encode("user2"),
            listOf(SimpleGrantedAuthority("ROLE_USER")),
            listOf(Group("group2"), Group("group3"))
        )

        val admin = KatalogUserDetailsHolder(
            "admin",
            passwordEncoder.encode("admin"),
            listOf(SimpleGrantedAuthority("ROLE_USER"), SimpleGrantedAuthority("ROLE_ADMIN")),
            listOf(Group("administrators"))
        )

        return ReactiveKatalogUserDetailsService(listOf(user, user2, admin))
    }

    @Bean
    fun groupService(): GroupService = object : GroupService {
        override suspend fun getAvailableGroups(): Collection<Group> {
            return listOf(
                Group("group1"),
                Group("group2"),
                Group("group3"),
                Group("administrators")
            )
        }
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