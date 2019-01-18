package com.bol.katalog.cqrs.clustering

import com.bol.katalog.cqrs.Command
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.Channel

interface ClusteringChannel {
    suspend fun <C : Command> sendCommand(command: C): Deferred<Unit>
    fun getChannel(): Channel<CompletableCommand>
    fun notifyChannelClosed()

    suspend fun stop(): Deferred<Unit>
}