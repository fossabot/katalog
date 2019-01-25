package com.bol.katalog.plugin.gcp

import com.bol.katalog.messaging.MessageBus
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.hasSize
import kotlin.random.Random
import kotlin.test.fail

@Disabled("Need to set up a new GCP project for testing purposes")
@ExtendWith(SpringExtension::class)
@SpringBootTest
class GcpMessageBusIT {
    @Autowired
    private lateinit var bus: MessageBus

    @Test
    fun `Can roundtrip messages`() {
        val messages = mutableListOf<MyTask>()

        repeat(3) {
            messages.add(MyTask("foo" + Random.nextInt()))
        }

        val received = mutableListOf<MyTask>()

        runBlocking {
            messages.forEach {
                bus.publish("queue", it)
            }

            while (received.size < messages.size) {
                bus.receive("queue") { message ->
                    received.add(message as MyTask)
                }
            }
        }

        expectThat(received).containsExactlyInAnyOrder(messages)

        // No more messages should be present on the queue after this
        runBlocking {
            bus.receive("queue") {
                fail("Should not have received any more messages")
            }
        }
    }

    @Test
    fun `Can retry messages`() {
        val received = mutableListOf<MyTask>()

        runBlocking {
            bus.publish("queue", MyTask("test"))

            repeat(5) {
                bus.receive("queue") {
                    throw UnsupportedOperationException("let's retry")
                }
            }

            bus.receive("queue") {
                received += it as MyTask
            }
        }

        expectThat(received).hasSize(1)

        // No more messages should be present on the queue after this
        runBlocking {
            bus.receive("queue") {
                fail("Should not have received any more messages")
            }
        }
    }

    data class MyTask(val data: String)
}
