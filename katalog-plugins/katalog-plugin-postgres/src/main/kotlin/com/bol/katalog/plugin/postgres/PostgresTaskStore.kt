package com.bol.katalog.plugin.postgres

import com.bol.katalog.store.TaskStore
import mu.KotlinLogging
import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.ActiveMQSession
import org.apache.activemq.broker.BrokerService
import org.apache.activemq.openwire.OpenWireFormat
import org.apache.activemq.store.jdbc.JDBCPersistenceAdapter
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PreDestroy
import javax.jms.*
import javax.sql.DataSource

class PostgresTaskStore(dataSource: DataSource) : TaskStore {
    private val log = KotlinLogging.logger {}

    private var broker: BrokerService = BrokerService()

    private var connection: Connection

    private var session: Session

    private var producers = ConcurrentHashMap<String, MessageProducer>()

    private var consumers = ConcurrentHashMap<String, MessageConsumer>()

    init {
        val jdbcAdapter = JDBCPersistenceAdapter(dataSource, OpenWireFormat())
        jdbcAdapter.isCreateTablesOnStartup = false

        broker.addConnector("vm://localhost")
        broker.persistenceAdapter = jdbcAdapter
        broker.isUseShutdownHook = false
        broker.start()

        val connectionFactory = ActiveMQConnectionFactory("vm://localhost?create=false")
        connection = connectionFactory.createConnection()
        connection.start()

        session = connection.createSession(false, ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE)
    }

    @PreDestroy
    fun preDestroy() {
        producers.values.forEach { it.close() }
        consumers.values.forEach { it.close() }
        connection.close()
        broker.stop()
    }

    override suspend fun publish(queue: String, task: Any) {
        val producer = producers.getOrPut(queue) {
            val destination = session.createQueue(queue)
            session.createProducer(destination)
        }

        producer.deliveryMode = DeliveryMode.PERSISTENT
        val message = session.createTextMessage(PostgresObjectMapper.get().writeValueAsString(task))
        message.setStringProperty("type", task::class.java.name)

        producer.send(message)
    }

    override suspend fun receive(queue: String, handler: (Any) -> Unit): Boolean {
        val consumer = consumers.getOrPut(queue) {
            val destination = session.createQueue(queue)
            session.createConsumer(destination)
        }

        val message = consumer.receive(100) ?: return false

        when (message) {
            is TextMessage -> {
                val clazz = Class.forName(message.getStringProperty("type"))
                val task = PostgresObjectMapper.get().readValue(message.text, clazz)

                try {
                    log.debug("Received message: {}", task)
                    handler(task)
                    message.acknowledge()

                    return true
                } catch (e: Exception) {
                    log.warn("Caught exception when handling message", e)
                }
            }
        }

        return false
    }
}