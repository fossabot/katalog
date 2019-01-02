package com.bol.katalog.config

import com.bol.katalog.config.security.SecurityConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "katalog")
class KatalogConfigurationProperties {
    var security = SecurityConfigurationProperties()
}