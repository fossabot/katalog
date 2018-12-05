package com.bol.katalog.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "katalog.security")
class SecurityConfigurationProperties {
    var token = TokenProperties()

    class TokenProperties {
        lateinit var hmacShaKey: String
    }
}