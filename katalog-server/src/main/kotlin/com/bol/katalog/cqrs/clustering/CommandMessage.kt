package com.bol.katalog.cqrs.clustering

import com.bol.katalog.cqrs.Command
import kotlinx.coroutines.CompletableDeferred

data class CommandMessage(
    val command: Command,
    val completion: CompletableDeferred<Unit> = CompletableDeferred()
)
