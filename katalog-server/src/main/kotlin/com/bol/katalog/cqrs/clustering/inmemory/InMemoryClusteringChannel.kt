package com.bol.katalog.cqrs.clustering.inmemory

import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.clustering.ClusteringChannel
import com.bol.katalog.cqrs.clustering.CommandMessage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.Channel

class InMemoryClusteringChannel : ClusteringChannel {
    private val channel = Channel<CommandMessage>()
    private val done = CompletableDeferred<Unit>()

    override suspend fun <C : Command> sendCommand(command: C): Deferred<Unit> {
        val complete = CompletableDeferred<Unit>()
        val message = CommandMessage(command, complete)
        channel.send(message)
        return complete
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