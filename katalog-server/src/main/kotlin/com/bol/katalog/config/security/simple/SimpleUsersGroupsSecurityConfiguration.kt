package com.bol.katalog.config.security.simple

import com.bol.katalog.config.KatalogConfigurationProperties
import com.bol.katalog.security.KatalogUserDetailsHolder
import com.bol.katalog.security.ReactiveKatalogUserDetailsService
import com.bol.katalog.security.SecurityProcessor
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import javax.annotation.PostConstruct

@Configuration
class SimpleUsersGroupsSecurityConfiguration {
    @Bean
    @ConditionalOnProperty("katalog.security.users.simple.enabled", matchIfMissing = false)
    fun userDetailsService(
        passwordEncoder: PasswordEncoder,
        config: KatalogConfigurationProperties
    ): ReactiveUserDetailsService {
        val users = config.security.users.simple.users.map { user ->
            KatalogUserDetailsHolder(
                id = user.value.username,
                username = user.value.username,
                password = passwordEncoder.encode(user.value.password),
                authorities = user.value.roles.map { SimpleGrantedAuthority("ROLE_$it") }
            )
        }

        return ReactiveKatalogUserDetailsService(users)
    }

    @Configuration
    @ConditionalOnProperty("katalog.security.groups.simple.enabled", matchIfMissing = false)
    class GroupInitializer {
        @Autowired
        private lateinit var security: SecurityProcessor

        @Autowired
        private lateinit var config: KatalogConfigurationProperties

        @PostConstruct
        fun createGroups() {
            runBlocking {
                config.security.groups.simple.groups.forEach { group ->
                    security.createGroup(group.key, group.value)
                }

                config.security.users.simple.users.forEach { user ->
                    val userId = user.key
                    user.value.groups.forEach { group ->
                        security.addUserToGroup(userId, group.key, group.value)
                    }
                }
            }
        }
    }
}
