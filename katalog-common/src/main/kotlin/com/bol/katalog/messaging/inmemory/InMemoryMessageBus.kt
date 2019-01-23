package com.bol.katalog.messaging.inmemory

import com.bol.katalog.messaging.MessageBus
import mu.KotlinLogging
import java.util.*

class InMemoryMessageBus : MessageBus {
    private val log = KotlinLogging.logger {}

    private val queues = mutableMapOf<String, Queue<Any>>()

    override suspend fun publish(queue: String, task: Any) {
        getQueue(queue).offer(task)
    }

    override suspend fun receive(queue: String, handler: suspend (Any) -> Unit) {
        val message = getQueue(queue).peek()
        if (message != null) {
            try {
                log.debug("Received message: {}", message)
                handler(message)

                // Remove message from queue
                getQueue(queue).poll()
            } catch (e: Exception) {
                log.warn("Caught exception when handling message", e)
            }
        }
    }

    private fun getQueue(queue: String) = queues.getOrPut(queue) { LinkedList<Any>() }
}