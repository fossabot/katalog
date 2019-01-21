package com.bol.katalog.plugin.atomix

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.util.*
import javax.validation.constraints.Min

@ConfigurationProperties(prefix = "katalog.clustering.atomix")
//@Validated
class AtomixProperties {
    //@Min(value = 2, message = "The cluster size needs to be at least 2")
    var clusterSize: Int = 0
    var memberId: String = "member-${UUID.randomUUID()}"
    var host: String = "localhost"
    var port: Int = 0
}