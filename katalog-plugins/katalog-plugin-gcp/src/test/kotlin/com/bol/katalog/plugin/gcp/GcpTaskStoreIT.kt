package com.bol.katalog.plugin.gcp

import com.bol.katalog.store.TaskStore
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import kotlin.random.Random
import kotlin.test.fail

@Ignore("Need to set up a new GCP project for testing purposes")
@RunWith(SpringRunner::class)
@SpringBootTest
class GcpTaskStoreIT {
    @Autowired
    private lateinit var taskStore: TaskStore

    @Test
    fun `Can roundtrip tasks`() {
        val messages = mutableListOf<MyTask>()

        repeat(3) {
            messages.add(MyTask("foo" + Random.nextInt()))
        }

        val received = mutableListOf<MyTask>()

        runBlocking {
            messages.forEach {
                taskStore.publish("queue", it)
            }

            while (received.size < messages.size) {
                taskStore.receive("queue") { message ->
                    received.add(message as MyTask)
                }
            }
        }

        expectThat(received).containsExactlyInAnyOrder(messages)

        // No more messages should be present on the queue after this
        runBlocking {
            taskStore.receive("queue") {
                fail("Should not have received any more messages")
            }
        }
    }

    @Test
    fun `Can retry tasks`() {
        runBlocking {
            taskStore.publish("queue", MyTask("test"))

            repeat(5) {
                taskStore.receive("queue") {
                    throw UnsupportedOperationException("let's retry")
                }
            }

            taskStore.receive("queue") {}
        }

        // No more messages should be present on the queue after this
        runBlocking {
            taskStore.receive("queue") {
                fail("Should not have received any more messages")
            }
        }
    }

    data class MyTask(val data: String)
}
