package com.bol.katalog.cqrs.clustering.inmemory

import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.clustering.ClusteringChannel
import com.bol.katalog.cqrs.clustering.CompletableCommand
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.Channel

class InMemoryClusteringChannel : ClusteringChannel {
    class InMemoryCompletableCommand(command: Command) : CompletableCommand(command) {
        val deferred = CompletableDeferred<Unit>()

        override fun complete(exception: Throwable?) {
            if (exception == null) {
                this.deferred.complete(Unit)
            } else {
                this.deferred.completeExceptionally(exception)
            }
        }
    }

    private val channel = Channel<CompletableCommand>()
    private val done = CompletableDeferred<Unit>()

    override suspend fun <C : Command> sendCommand(command: C): Deferred<Unit> {
        val message = InMemoryCompletableCommand(command)
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