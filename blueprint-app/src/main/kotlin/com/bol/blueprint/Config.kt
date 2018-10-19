package com.bol.blueprint

import com.bol.blueprint.config.fallback
import com.bol.blueprint.domain.*
import com.bol.blueprint.store.BlobStore
import com.bol.blueprint.store.EventStore
import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.InMemoryEventStore
import kotlinx.coroutines.experimental.runBlocking
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository
import org.springframework.session.ReactiveMapSessionRepository
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession
import org.springframework.web.server.session.HeaderWebSessionIdResolver
import org.springframework.web.server.session.WebSessionIdResolver
import reactor.core.publisher.Mono
import javax.annotation.PostConstruct

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
    @ConditionalOnProperty("blueprint.test-data.enabled")
    class TestDataConfiguration {
        @Autowired
        lateinit var commandHandler: CommandHandler

        @PostConstruct
        fun init() {
            runBlocking {
                with(commandHandler) {
                    /*createNamespace(NamespaceKey("ns1"), GroupKey("group1"))
                    createNamespace(NamespaceKey("ns2"), GroupKey("group1"))
                    createNamespace(NamespaceKey("ns3"), GroupKey("group1"))
                    createSchema(SchemaKey("ns1", "schema1"), SchemaType.default())
                    createSchema(SchemaKey("ns1", "schema2"), SchemaType.default())
                    createSchema(SchemaKey("ns1", "schema3"), SchemaType.default())
                    createSchema(SchemaKey("ns1", "schema4"), SchemaType.default())
                    createSchema(SchemaKey("ns1", "schema5"), SchemaType.default())
                    createSchema(SchemaKey("ns2", "schema1"), SchemaType.default())
                    createSchema(SchemaKey("ns2", "schema2"), SchemaType.default())
                    createVersion(VersionKey("ns1", "schema1", "1.0.0"))
                    createVersion(VersionKey("ns1", "schema1", "1.0.1"))
                    createVersion(VersionKey("ns1", "schema1", "2.0.0-SNAPSHOT"))
                    createArtifact(ArtifactKey("ns1", "schema1", "1.0.0", "artifact1.json"), MediaType.JSON, byteArrayOf(1, 2, 3))
                    createArtifact(ArtifactKey("ns1", "schema1", "1.0.0", "artifact2.json"), MediaType.JSON, byteArrayOf(4, 5, 6))*/
                }
            }
        }
    }

    @Configuration
    @EnableSpringWebSession
    @EnableWebFluxSecurity
    class SecurityConfiguration {
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
                .logout().logoutUrl("/api/v1/auth/logout").logoutSuccessHandler { _, _ -> Mono.empty<Void>() }
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
        fun userDetailsProvider(): CurrentUserSupplier = ReactiveSecurityContextCurrentUserSupplier()
    }

    @Configuration
    @ConditionalOnProperty("blueprint.security.simple.enabled", matchIfMissing = false)
    class SecurityFallbackConfiguration {
        @Bean
        fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

        @Bean
        fun userDetailsService(config: BlueprintConfigurationProperties): ReactiveUserDetailsService {
            val users = config.security.simple.users.map { user ->
                BlueprintUserDetails(
                        username = user.value.username,
                        password = passwordEncoder().encode(user.value.password),
                        authorities = user.value.roles.map { SimpleGrantedAuthority("ROLE_$it") },
                        groups = user.value.groups.map { Group(it) }
                )
            }

            return ReactiveBlueprintUserDetailsService(users)
        }
    }
}