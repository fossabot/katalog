package com.bol.katalog.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.ServerSocket
import java.util.*

@ConfigurationProperties("katalog.clustering.hazelcast")
class HazelcastProperties {
    var enabled: Boolean = false
    var instanceName: String = "member-${UUID.randomUUID()}"
    var port: Int = 0
    var members: List<String> = listOf()

    fun portOrRandomFree(): Int {
        return if (port == 0) {
            ServerSocket(0).use { it.localPort }
        } else {
            port
        }
    }
}