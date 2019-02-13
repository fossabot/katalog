package com.bol.katalog.config

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.hazelcast.HazelcastKryoSerializer
import com.bol.katalog.store.EventStore
import com.hazelcast.config.Config
import com.hazelcast.config.SerializerConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.ReactiveMapSessionRepository
import org.springframework.session.Session
import java.time.Clock
import javax.annotation.PostConstruct

@Configuration
@EnableConfigurationProperties(HazelcastProperties::class)
@ConditionalOnProperty("katalog.clustering.hazelcast.enabled", havingValue = "true", matchIfMissing = true)
class HazelcastAutoConfiguration {
    private val log = KotlinLogging.logger {}

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    @ConditionalOnProperty("katalog.clustering.type", havingValue = "hazelcast")
    fun hazelcastInstance(properties: HazelcastProperties): HazelcastInstance {
        val config = Config()
        config.setProperty("hazelcast.logging.type", "slf4j")
        config.groupConfig.name = "katalog"
        config.instanceName = properties.instanceName
        with(config.networkConfig) {
            this.port = properties.portOrRandomFree()
            join.multicastConfig.isEnabled = false
            join.tcpIpConfig.isEnabled = true
            join.tcpIpConfig.members = properties.members
        }
        val sc = SerializerConfig()
        sc.implementation = HazelcastKryoSerializer()
        sc.typeClass = Any::class.java
        config.serializationConfig.addSerializerConfig(sc)

        return Hazelcast.newHazelcastInstance(config)
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    @ConditionalOnProperty("katalog.clustering.type", havingValue = "standalone")
    fun standaloneHazelcastInstance(properties: HazelcastProperties): HazelcastInstance {
        val config = Config()

        // Logging
        config.setProperty("hazelcast.logging.type", "slf4j")

        // Cluster joining
        config.instanceName = "member-1"
        config.networkConfig.port = properties.portOrRandomFree()
        config.networkConfig.join.multicastConfig.isEnabled = false
        config.networkConfig.join.awsConfig.isEnabled = false
        config.networkConfig.join.tcpIpConfig.isEnabled = false

        // Kryo Serializer
        val sc = SerializerConfig()
        sc.implementation = HazelcastKryoSerializer()
        sc.typeClass = Any::class.java
        config.serializationConfig.addSerializerConfig(sc)

        // Create instance and invoke block
        return Hazelcast.newHazelcastInstance(config)
    }

    @Bean
    fun hazelcastStartupRunnerManager(
        hazelcast: HazelcastInstance,
        startupRunners: List<StartupRunner>
    ): StartupRunnerManager {
        return HazelcastStartupRunnerManager(hazelcast, startupRunners)
    }

    @Bean
    fun hazelcastAggregateContext(
        hazelcast: HazelcastInstance,
        eventStore: EventStore,
        clock: Clock
    ): AggregateContext {
        return AggregateContext(hazelcast, eventStore, clock)
    }

    @Bean
    fun hazelcastSessionRepository(hazelcast: HazelcastInstance) =
        ReactiveMapSessionRepository(hazelcast.getMap<String, Session>("user-sessions"))

    @Bean
    fun apiStartupWebFilter(startupRunnerManager: StartupRunnerManager) = ApiStartupWebFilter(startupRunnerManager)

    @PostConstruct
    fun init() {
        val properties = applicationContext.getBean<HazelcastProperties>()
        if (properties.members.isNotEmpty()) {
            val hazelcast = applicationContext.getBean<HazelcastInstance>()
            waitForCluster(hazelcast, properties.members.size)
        }
    }

    fun waitForCluster(hazelcast: HazelcastInstance, expectedSize: Int) {
        while (true) {
            val reachable = hazelcast.cluster.members.size
            if (reachable == expectedSize) {
                break
            }

            log.info("Waiting for cluster to form... Expected size: $expectedSize, actual size: $reachable")
            Thread.sleep(1000)
        }
        log.info("Cluster has formed")
    }
}