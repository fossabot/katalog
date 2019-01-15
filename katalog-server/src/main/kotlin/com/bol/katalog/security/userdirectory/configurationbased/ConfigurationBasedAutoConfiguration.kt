package com.bol.katalog.security.userdirectory.configurationbased

import com.bol.katalog.config.KatalogConfigurationProperties
import com.bol.katalog.users.UserDirectory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class ConfigurationBasedAutoConfiguration {
    @Bean
    fun configurationBasedUserDirectory(
        config: KatalogConfigurationProperties,
        passwordEncoder: PasswordEncoder
    ): UserDirectory {
        return ConfigurationBasedUserDirectory(config.security, passwordEncoder)
    }
}