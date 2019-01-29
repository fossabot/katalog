package com.bol.katalog.plugin.atomix

import com.bol.katalog.config.StartupRunner
import com.bol.katalog.config.StartupRunnerManager
import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.store.EventStore
import io.atomix.cluster.discovery.MulticastDiscoveryProvider
import io.atomix.cluster.discovery.NodeDiscoveryProvider
import io.atomix.core.Atomix
import io.atomix.primitive.partition.ManagedPartitionGroup
import io.atomix.protocols.backup.partition.PrimaryBackupPartitionGroup
import io.atomix.utils.net.Address
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
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
@ConditionalOnProperty("katalog.clustering.type", havingValue = "atomix")
class AtomixAutoConfiguration {
    private val log = KotlinLogging.logger {}

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
            .withManagementGroup(atomixSystemPartitionGroup(properties))
            .withPartitionGroups(atomixDataPartitionGroup(properties))
            .build()
    }

    @Bean
    fun atomixNodeDiscoveryProvider(): NodeDiscoveryProvider {
        return MulticastDiscoveryProvider.builder().build()
    }

    @Bean
    fun atomixSystemPartitionGroup(properties: AtomixProperties): ManagedPartitionGroup {
        return PrimaryBackupPartitionGroup.builder("system")
            .withNumPartitions(properties.members.size)
            .build()
    }

    @Bean
    fun atomixDataPartitionGroup(properties: AtomixProperties): ManagedPartitionGroup {
        return PrimaryBackupPartitionGroup.builder("data")
            .withNumPartitions(properties.members.size)
            .build()
    }

    @Bean
    fun atomixAggregateContext(atomix: Atomix, eventStore: EventStore, clock: Clock): AggregateContext {
        return AtomixAggregateContext(atomix, eventStore, clock)
    }

    @Bean
    fun atomixStartupRunnerManager(atomix: Atomix, startupRunners: List<StartupRunner>): StartupRunnerManager {
        return AtomixStartupRunnerManager(atomix, startupRunners)
    }

    @PostConstruct
    fun init() {
        val properties = applicationContext.getBean<AtomixProperties>()
        val atomix = applicationContext.getBean<Atomix>()
        atomix.start().join()

        while (true) {
            val reachable = atomix.membershipService.reachableMembers.map { it.id().id() }
            val expected = properties.members
            val stillWaitingFor = expected - reachable
            if (stillWaitingFor.isEmpty()) break

            log.info("Waiting for cluster to form... Members not yet reachable: $stillWaitingFor")
            Thread.sleep(1000)
        }
        log.info("Cluster has formed")
    }

    @PreDestroy
    fun destroy() {
        val atomix = applicationContext.getBean<Atomix>()
        atomix.stop().join()
    }
}