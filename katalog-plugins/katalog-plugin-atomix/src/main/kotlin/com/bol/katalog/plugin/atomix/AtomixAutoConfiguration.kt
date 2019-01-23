package com.bol.katalog.plugin.atomix

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.store.EventStore
import io.atomix.cluster.discovery.MulticastDiscoveryProvider
import io.atomix.cluster.discovery.NodeDiscoveryProvider
import io.atomix.core.Atomix
import io.atomix.primitive.partition.ManagedPartitionGroup
import io.atomix.protocols.backup.partition.PrimaryBackupPartitionGroup
import io.atomix.utils.net.Address
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.ServerSocket
import java.time.Clock
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Configuration
@EnableConfigurationProperties(AtomixProperties::class)
@ConditionalOnMissingBean(Atomix::class)
class AtomixAutoConfiguration {
    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Bean
    fun atomix(properties: AtomixProperties): Atomix {
        val port = if (properties.port == 0) {
            ServerSocket(0).use { it.localPort }
        } else {
            properties.port
        }

        return Atomix.builder()
            .withAddress(Address.from("${properties.host}:$port"))
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

    @Bean
    fun atomixAggregateContext(atomix: Atomix, eventStore: EventStore, clock: Clock): AggregateContext {
        return AtomixAggregateContext(atomix, eventStore, clock)
    }

    @PostConstruct
    fun init() {
        val atomix = applicationContext.getBean<Atomix>()
        atomix.start().join()

        val context = applicationContext.getBean<AggregateContext>() as AtomixAggregateContext
        context.invokeStartupBlocks()
    }

    @PreDestroy
    fun destroy() {
        val atomix = applicationContext.getBean<Atomix>()
        atomix.stop().join()
    }
}