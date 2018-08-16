package com.bol.blueprint

import com.bol.blueprint.config.fallback
import com.bol.blueprint.store.BlobStore
import com.bol.blueprint.store.EventStore
import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.InMemoryEventStore
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.session.MapSessionRepository
import org.springframework.session.Session
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer
import org.springframework.session.web.http.CookieSerializer
import org.springframework.session.web.http.DefaultCookieSerializer
import java.util.concurrent.ConcurrentHashMap

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
    class SecurityFallbackConfiguration {
        @Bean
        @ConditionalOnMissingBean
        fun webSecurity(properties: BlueprintConfigurationProperties): WebSecurity {
            return WebSecurity(passwordEncoder(), properties)
        }

        @Bean
        @ConditionalOnMissingBean
        fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

        @Configuration
        @EnableSpringHttpSession
        class SessionConfig : AbstractHttpSessionApplicationInitializer() {
            @Bean
            @ConditionalOnMissingBean
            fun cookieSerializer(): CookieSerializer {
                val serializer = DefaultCookieSerializer()
                serializer.setCookieName("BLUEPRINT")
                serializer.setCookiePath("/")
                serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$")
                return serializer
            }

            @Bean
            @ConditionalOnMissingBean
            fun sessionRepository(): MapSessionRepository {
                return MapSessionRepository(ConcurrentHashMap<String, Session>())
            }
        }

        class WebSecurity(private val passwordEncoder: PasswordEncoder, private val properties: BlueprintConfigurationProperties) : WebSecurityConfigurerAdapter() {
            @Autowired
            fun configureGlobal(auth: AuthenticationManagerBuilder) {
                val builder = auth.inMemoryAuthentication()

                properties.security.simple.users.values.forEach {
                    builder
                        .withUser(it.username)
                        .password(passwordEncoder.encode(it.password))
                        .roles(*it.roles.toTypedArray())
                }
            }

            override fun configure(http: HttpSecurity) {
                http
                    .antMatcher("/api/**")
                    .csrf().disable()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .and()
                    .authorizeRequests().antMatchers("/api/**").hasAnyRole("USER", "ADMIN").anyRequest().authenticated()
                    .and()
                    .httpBasic()
            }
        }
    }
}