package com.bol.katalog.plugin.atomix

/*
import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.clustering.ClusteringChannel
import com.bol.katalog.cqrs.clustering.HandleableCommand
import io.atomix.core.Atomix
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.Channel

class AtomixClusteringChannel(private val atomix: Atomix) : ClusteringChannel {
    override suspend fun <C : Command> sendCommand(command: C): Deferred<Unit> {
        throw NotImplementedError()
    }

    override fun getChannel(): Channel<HandleableCommand> {
        throw NotImplementedError()
    }

    override fun notifyChannelClosed() {
        throw NotImplementedError()
    }

    override suspend fun stop(): Deferred<Unit> {
        throw NotImplementedError()
    }
}
*/