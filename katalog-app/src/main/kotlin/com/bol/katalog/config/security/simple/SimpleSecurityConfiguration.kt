package com.bol.katalog.config.security.simple

import com.bol.katalog.config.KatalogConfigurationProperties
import com.bol.katalog.domain.Group
import com.bol.katalog.security.KatalogUserDetailsHolder
import com.bol.katalog.security.ReactiveKatalogUserDetailsService
import com.bol.katalog.security.groups.GroupService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@ConditionalOnProperty("katalog.security.simple.enabled", matchIfMissing = false)
class SimpleSecurityConfiguration {
    @Bean
    fun userDetailsService(
        passwordEncoder: PasswordEncoder,
        config: KatalogConfigurationProperties
    ): ReactiveUserDetailsService {
        val users = config.security.simple.users.map { user ->
            KatalogUserDetailsHolder(
                username = user.value.username,
                password = passwordEncoder.encode(user.value.password),
                authorities = user.value.roles.map { SimpleGrantedAuthority("ROLE_$it") },
                groups = user.value.groups.map { Group(it) }
            )
        }

        return ReactiveKatalogUserDetailsService(users)
    }

    @Bean
    fun groupService(
        config: KatalogConfigurationProperties
    ): GroupService {
        return object : GroupService {
            override suspend fun getAvailableGroups(): Collection<Group> {
                return config.security.simple.groups.map { Group(it) }
            }
        }
    }
}
