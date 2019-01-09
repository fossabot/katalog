package com.bol.katalog.plugin.azure

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AzureProperties::class)
class AzureAutoConfiguration {
}