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
import java.time.Clock
import java.util.*
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

    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Configuration
    @ConditionalOnProperty("blueprint.test-data.enabled")
    class TestDataConfiguration {
        @Autowired
        lateinit var commandHandler: CommandHandler

        @PostConstruct
        fun init() {
            val ns1: NamespaceId = UUID.randomUUID()
            val ns2: NamespaceId = UUID.randomUUID()
            val group1: GroupId = UUID.randomUUID()
            val ns1_schema1: SchemaId = UUID.randomUUID()
            val ns1_schema2: SchemaId = UUID.randomUUID()
            val ns2_schema3: SchemaId = UUID.randomUUID()
            val ns1_schema1_v100: VersionId = UUID.randomUUID()
            val ns1_schema1_v101: VersionId = UUID.randomUUID()
            val ns1_schema1_v110snapshot: VersionId = UUID.randomUUID()
            val ns1_schema1_v200snapshot: VersionId = UUID.randomUUID()
            val ns2_schema3_v100: VersionId = UUID.randomUUID()
            val artifact1: ArtifactId = UUID.randomUUID()
            val artifact2: ArtifactId = UUID.randomUUID()

            runBlocking {
                with(commandHandler) {
                    createNamespace(ns1, group1, "ns1")
                    createNamespace(ns2, group1, "ns2")

                    createSchema(ns1, ns1_schema1, "schema1", SchemaType.default())
                    createSchema(ns1, ns1_schema2, "schema2", SchemaType.default())
                    createSchema(ns2, ns2_schema3, "schema3", SchemaType.default())

                    createVersion(ns1_schema1, ns1_schema1_v100, "1.0.0")
                    createVersion(ns1_schema1, ns1_schema1_v101, "1.0.1")
                    createVersion(ns1_schema1, ns1_schema1_v110snapshot, "1.1.0-SNAPSHOT")
                    createVersion(ns1_schema1, ns1_schema1_v200snapshot, "2.0.0-SNAPSHOT")

                    createVersion(ns2_schema3, ns2_schema3_v100, "1.0.0")

                    createArtifact(ns1_schema1_v100, artifact1, "artifact1.json", MediaType.JSON, byteArrayOf(1, 2, 3))
                    createArtifact(ns1_schema1_v101, artifact2, "artifact2.json", MediaType.JSON, byteArrayOf(4, 5, 6))
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