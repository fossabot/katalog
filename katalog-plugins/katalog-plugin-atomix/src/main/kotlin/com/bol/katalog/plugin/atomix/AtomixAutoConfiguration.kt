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
import java.util.*

@Configuration
@EnableConfigurationProperties(AtomixProperties::class)
@ConditionalOnMissingBean(Atomix::class)
class AtomixAutoConfiguration {
    @Bean
    fun atomix(): Atomix {
        return Atomix.builder()
            .withAddress("127.0.0.1:5001")
            .withMemberId("member-${UUID.randomUUID()}")
            .withMulticastEnabled()
            .withMembershipProvider(atomixNodeDiscoveryProvider())
            .withManagementGroup(atomixManagementGroup())
            .withPartitionGroups(atomixPartitionGroups())
            .build()
    }

    @Bean
    fun atomixNodeDiscoveryProvider(): NodeDiscoveryProvider {
        return MulticastDiscoveryProvider.builder()
            .build()
    }

    @Bean
    fun atomixManagementGroup(): ManagedPartitionGroup {
        return PrimaryBackupPartitionGroup.builder("system")
            .withNumPartitions(2)
            .build()
    }

    @Bean
    fun atomixPartitionGroups(): List<ManagedPartitionGroup> {
        return emptyList()
    }
}