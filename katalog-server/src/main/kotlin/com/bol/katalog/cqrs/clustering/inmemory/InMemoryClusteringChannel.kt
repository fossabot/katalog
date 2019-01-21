package com.bol.katalog.cqrs.clustering.inmemory

import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.clustering.ClusteringChannel
import com.bol.katalog.cqrs.clustering.HandleableCommand
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.Channel

class InMemoryClusteringChannel : ClusteringChannel {
    class InMemoryHandleableCommand(private val command: Command) : HandleableCommand {
        internal val deferred = CompletableDeferred<Unit>()

        override suspend fun handle(block: suspend (Command) -> Unit) {
            try {
                block(command)
                deferred.complete(Unit)
            } catch (e: Throwable) {
                this.deferred.completeExceptionally(e)
            }
        }
    }

    private val channel = Channel<HandleableCommand>()
    private val done = CompletableDeferred<Unit>()

    override suspend fun <C : Command> sendCommand(command: C): Deferred<Unit> {
        val message = InMemoryClusteringChannel.InMemoryHandleableCommand(command)
        channel.send(message)
        return message.deferred
    }

    override fun getChannel() = channel

    override fun notifyChannelClosed() {
        done.complete(Unit)
    }

    override suspend fun stop(): Deferred<Unit> {
        channel.close()
        return done
    }
}