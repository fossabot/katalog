package com.bol.katalog.cqrs

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class InMemoryCommandQueue(
    private val onCommand: suspend (Command, Command.Metadata) -> Command.Result
) : AutoCloseable {
    data class QueueItem(
        val command: Command,
        val metadata: Command.Metadata,
        val complete: CompletableDeferred<Command.Result>
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
                    val result: Command.Result = onCommand(message.command, message.metadata)
                    message.complete.complete(result)
                } catch (e: Throwable) {
                    message.complete.completeExceptionally(e)
                }
            }

            done.complete(Unit)
        }
    }

    suspend fun send(command: Command, metadata: Command.Metadata): Command.Result {
        val complete = CompletableDeferred<Command.Result>()
        channel.send(QueueItem(command, metadata, complete))
        return complete.await()
    }

    override fun close() {
        runBlocking {
            channel.close()
            done.await()
        }
    }
}
