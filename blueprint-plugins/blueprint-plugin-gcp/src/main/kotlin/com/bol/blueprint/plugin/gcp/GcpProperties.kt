package com.bol.blueprint.plugin.gcp

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotEmpty

@ConfigurationProperties(prefix = "blueprint.gcp")
@Validated
class GcpProperties {
    @NotEmpty(message = "Bucket name is required")
    lateinit var bucketName: String
}