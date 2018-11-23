package com.bol.katalog.plugin.gcp

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotEmpty

@ConfigurationProperties(prefix = "katalog.gcp")
@Validated
class GcpProperties {
    @NotEmpty(message = "Bucket name is required")
    lateinit var bucketName: String
}