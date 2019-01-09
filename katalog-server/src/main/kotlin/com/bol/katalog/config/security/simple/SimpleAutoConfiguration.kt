package com.bol.katalog.config.security.simple

import com.bol.katalog.config.KatalogConfigurationProperties
import com.bol.katalog.users.UserDirectory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@ConditionalOnProperty("katalog.security.simple-user-directory.enabled", havingValue = "true", matchIfMissing = false)
class SimpleAutoConfiguration {
    @Bean
    fun simpleUserDirectory(
        config: KatalogConfigurationProperties,
        passwordEncoder: PasswordEncoder
    ): UserDirectory {
        return SimpleUserDirectory(config.security.simpleUserDirectory, passwordEncoder)
    }
}