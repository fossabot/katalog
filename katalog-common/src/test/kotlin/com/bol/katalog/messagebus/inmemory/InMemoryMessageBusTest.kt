package com.bol.katalog.messagebus.inmemory

import com.bol.katalog.messaging.inmemory.InMemoryMessageBus
import kotlinx.coroutines.runBlocking
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.hasSize
import kotlin.random.Random
import kotlin.test.fail

class InMemoryMessageBusTest {
    private val bus = InMemoryMessageBus()

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