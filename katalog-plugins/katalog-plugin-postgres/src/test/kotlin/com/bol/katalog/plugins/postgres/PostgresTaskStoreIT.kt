package com.bol.katalog.plugins.postgres

import com.bol.katalog.store.TaskStore
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isFalse
import kotlin.random.Random

@RunWith(SpringRunner::class)
@SpringBootTest
@Transactional
class PostgresTaskStoreIT {
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

            repeat(3) {
                taskStore.receive("queue") { message ->
                    received.add(message as MyTask)
                }
            }
        }

        expectThat(received).containsExactlyInAnyOrder(messages)

        // No more messages should be present on the queue after this
        val moreMessages = runBlocking {
            taskStore.receive("queue") {}
        }
        expectThat(moreMessages).isFalse()
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
        val moreMessages = runBlocking {
            taskStore.receive("queue") {}
        }
        expectThat(moreMessages).isFalse()
    }

    data class MyTask(val data: String)
}
