package com.bol.katalog.plugin.azure

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@EnableConfigurationProperties(AzureProperties::class)
@Import(
    GraphClient::class,
    AzureGraphUserDirectory::class
)
class AzureAutoConfiguration