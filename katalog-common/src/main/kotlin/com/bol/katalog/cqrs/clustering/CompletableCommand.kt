package com.bol.katalog.cqrs.clustering

import com.bol.katalog.cqrs.Command

abstract class CompletableCommand(val command: Command) : Command by command {
    abstract fun complete(exception: Throwable? = null)
}