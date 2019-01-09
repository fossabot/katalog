package com.bol.katalog.plugin.postgres

import com.bol.katalog.messaging.MessageBus
import mu.KotlinLogging
import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.ActiveMQSession
import org.apache.activemq.broker.BrokerService
import org.apache.activemq.openwire.OpenWireFormat
import org.apache.activemq.store.jdbc.JDBCPersistenceAdapter
import javax.annotation.PreDestroy
import javax.jms.*
import javax.sql.DataSource
import org.apache.activemq.RedeliveryPolicy

class PostgresMessageBus(dataSource: DataSource) : MessageBus {
    private val log = KotlinLogging.logger {}

    private var broker: BrokerService = BrokerService()

    private var connection: Connection

    init {
        val jdbcAdapter = JDBCPersistenceAdapter(dataSource, OpenWireFormat())
        jdbcAdapter.isCreateTablesOnStartup = false

        broker.addConnector("vm://localhost")
        broker.persistenceAdapter = jdbcAdapter
        broker.isUseShutdownHook = false
        broker.start()

        val policy = RedeliveryPolicy()
        policy.initialRedeliveryDelay = 100L
        policy.maximumRedeliveries = RedeliveryPolicy.NO_MAXIMUM_REDELIVERIES

        val connectionFactory = ActiveMQConnectionFactory("vm://localhost?create=false")
        connectionFactory.redeliveryPolicy = policy
        connectionFactory.isUseRetroactiveConsumer = true
        connection = connectionFactory.createConnection()
        connection.start()
    }

    @PreDestroy
    fun preDestroy() {
        connection.close()
        broker.stop()
    }

    override suspend fun publish(queue: String, task: Any) {
        val session = connection.createSession(true, ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE)
        try {
            val destination = session.createQueue(queue)
            val producer = session.createProducer(destination)

            try {
                producer.deliveryMode = DeliveryMode.PERSISTENT
                val message = session.createTextMessage(PostgresObjectMapper.get().writeValueAsString(task))
                message.setStringProperty("type", task::class.java.name)

                producer.send(message)
                session.commit()

            } finally {
                producer.close()
            }
        } finally {
            session.close()
        }
    }

    override suspend fun receive(queue: String, handler: suspend (Any) -> Unit) {
        val session = connection.createSession(true, ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE)
        try {
            val destination = session.createQueue(queue)
            val consumer = session.createConsumer(destination)

            try {
                val message = consumer.receive(1000)
                if (message != null) {
                    when (message) {
                        is TextMessage -> {
                            val clazz = Class.forName(message.getStringProperty("type"))
                            val task = PostgresObjectMapper.get().readValue(message.text, clazz)

                            try {
                                log.debug("Received message: {}", task)
                                handler(task)
                                session.commit()
                            } catch (e: Exception) {
                                log.warn("Caught exception when handling message", e)
                                session.rollback()
                            }
                        }
                        else -> throw UnsupportedOperationException("Unknown message type: $message")
                    }
                }
            } finally {
                consumer.close()
            }
        } finally {
            session.close()
        }
    }
}