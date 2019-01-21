package com.bol.katalog.cqrs.clustering

import com.bol.katalog.cqrs.Command

interface HandleableCommand {
    suspend fun handle(block: suspend (Command) -> Unit)
}