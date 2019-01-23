package com.bol.katalog.messaging.inmemory

import com.bol.katalog.cqrs.Command
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class InMemoryQueue<T>(
    private val onCommand: suspend (T) -> Command.Result
) : AutoCloseable {
    data class QueueItem<T>(val item: T, val complete: CompletableDeferred<Command.Result>)

    private val channel = Channel<QueueItem<T>>()
    private val done = CompletableDeferred<Unit>()

    init {
        startListenerThread()
    }

    private fun startListenerThread() {
        GlobalScope.launch {
            for (message in channel) {
                try {
                    val result: Command.Result = onCommand(message.item)
                    message.complete.complete(result)
                } catch (e: Throwable) {
                    message.complete.complete(Command.UnknownFailure(e.message))
                }
            }

            done.complete(Unit)
        }
    }

    suspend fun send(item: T): Command.Result {
        val complete = CompletableDeferred<Command.Result>()
        channel.send(QueueItem(item, complete))
        return complete.await()
    }

    override fun close() {
        runBlocking {
            channel.close()
            done.await()
        }
    }
}
