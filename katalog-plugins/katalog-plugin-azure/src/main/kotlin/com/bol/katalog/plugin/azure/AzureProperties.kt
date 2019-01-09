package com.bol.katalog.plugin.azure

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotEmpty

@ConfigurationProperties(prefix = "katalog.azure")
@Validated
class AzureProperties {
    var graphApi = GraphProperties()

    class GraphProperties {
        @NotEmpty(message = "Authority is required")
        lateinit var authority: String

        @NotEmpty(message = "Client ID is required")
        lateinit var clientId: String

        @NotEmpty(message = "Client secret is required")
        lateinit var clientSecret: String
    }
}