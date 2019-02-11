package com.bol.katalog.cqrs.hazelcast

import com.bol.katalog.cqrs.Command
import com.bol.katalog.users.UserId
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class InMemoryCommandQueue(
    private val onCommand: suspend (Command, UserId) -> Unit
) : AutoCloseable {
    data class QueueItem(
        val command: Command,
        val userId: UserId,
        val complete: CompletableDeferred<Unit>
    )

    private val channel = Channel<QueueItem>()
    private val done = CompletableDeferred<Unit>()

    init {
        startListenerThread()
    }

    private fun startListenerThread() {
        GlobalScope.launch {
            for (message in channel) {
                try {
                    onCommand(message.command, message.userId)
                    message.complete.complete(Unit)
                } catch (e: Throwable) {
                    message.complete.completeExceptionally(e)
                }
            }

            done.complete(Unit)
        }
    }

    suspend fun send(command: Command, userId: UserId) {
        val complete = CompletableDeferred<Unit>()
        channel.send(QueueItem(command, userId, complete))
        return complete.await()
    }

    override fun close() {
        runBlocking {
            channel.close()
            done.await()
        }
    }
}