package com.bol.katalog.plugin.atomix

import io.atomix.utils.net.Address
import org.springframework.boot.context.properties.ConfigurationProperties
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

@ConfigurationProperties(prefix = "katalog.clustering.atomix")
class AtomixProperties {
    var memberId: String = "member-${UUID.randomUUID()}"
    var host: String = "localhost"
    var port: Int = 0
    var members: List<String> = listOf()
    var nodeAddresses: List<Address> = listOf()
    var protocol: AtomixProtocol = AtomixProtocol.PRIMARY_BACKUP
    var dataDirectory: Path = Paths.get("/tmp/katalog/atomix")

    enum class AtomixProtocol(val value: String) {
        RAFT("Raft"),
        PRIMARY_BACKUP("Primary Backup")
    }
}