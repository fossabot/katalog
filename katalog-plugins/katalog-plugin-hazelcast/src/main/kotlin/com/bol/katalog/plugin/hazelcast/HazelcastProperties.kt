package com.bol.katalog.plugin.hazelcast

import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.*

@ConfigurationProperties("katalog.clustering.hazelcast")
class HazelcastProperties {
    var instanceName: String = "member-${UUID.randomUUID()}"
    var port: Int = 0
    var members: List<String> = listOf()
}