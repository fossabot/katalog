package com.bol.katalog.plugin.atomix

import io.atomix.cluster.discovery.MulticastDiscoveryProvider
import io.atomix.cluster.discovery.NodeDiscoveryProvider
import io.atomix.core.Atomix
import io.atomix.primitive.partition.ManagedPartitionGroup
import io.atomix.protocols.backup.partition.PrimaryBackupPartitionGroup
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.ServerSocket

@Configuration
@EnableConfigurationProperties(AtomixProperties::class)
@ConditionalOnMissingBean(Atomix::class)
class AtomixAutoConfiguration {
    @Bean
    fun atomix(properties: AtomixProperties): Atomix {
        val port = if (properties.port == 0) {
            ServerSocket(0).use { it.localPort }
        } else {
            properties.port
        }

        return Atomix.builder()
            .withAddress("${properties.host}:$port")
            .withMemberId(properties.memberId)
            .withMulticastEnabled()
            .withMembershipProvider(atomixNodeDiscoveryProvider())
            .withManagementGroup(atomixSystemPartitionGroup())
            .withPartitionGroups(listOf(atomixDataPartitionGroup()))
            .build()
    }

    @Bean
    fun atomixNodeDiscoveryProvider(): NodeDiscoveryProvider {
        return MulticastDiscoveryProvider.builder()
            .build()
    }

    @Bean
    fun atomixSystemPartitionGroup(): ManagedPartitionGroup {
        return PrimaryBackupPartitionGroup.builder("system")
            .withNumPartitions(32)
            .build()
    }

    @Bean
    fun atomixDataPartitionGroup(): ManagedPartitionGroup {
        return PrimaryBackupPartitionGroup.builder("data")
            .withNumPartitions(32)
            .build()
    }
}