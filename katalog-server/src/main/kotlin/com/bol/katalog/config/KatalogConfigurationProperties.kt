package com.bol.katalog.config

import com.bol.katalog.security.config.SecurityConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "katalog")
class KatalogConfigurationProperties {
    var security = SecurityConfigurationProperties()
}