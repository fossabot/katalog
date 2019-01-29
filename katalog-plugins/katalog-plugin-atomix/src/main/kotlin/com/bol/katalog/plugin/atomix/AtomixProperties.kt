package com.bol.katalog.plugin.atomix

import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.*

@ConfigurationProperties(prefix = "katalog.clustering.atomix")
class AtomixProperties {
    var memberId: String = "member-${UUID.randomUUID()}"
    var host: String = "localhost"
    var port: Int = 0
    var members: MutableList<String> = mutableListOf()
}