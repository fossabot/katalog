package com.bol.katalog.cqrs.hazelcast

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.utils.CoroutineLocal

object CoroutineTransactionContext {
    private val current = CoroutineLocal<Transaction>()

    suspend fun get() = current.get()
    suspend fun set(transaction: Transaction?) {
        this.current.set(transaction)
    }
}

suspend fun <T> transaction(context: AggregateContext, block: suspend () -> T): T {
    if (CoroutineTransactionContext.get() != null) throw IllegalStateException("Already an active transaction")

    val tx = Transaction(context)
    CoroutineTransactionContext.set(tx)
    try {
        val result = block()
        tx.commit()
        return result
    } catch (e: Throwable) {
        tx.rollback()
        throw e
    } finally {
        CoroutineTransactionContext.set(null)
    }
}

suspend fun <T> transactionIfNeeded(context: AggregateContext, block: suspend () -> T): T {
    return if (CoroutineTransactionContext.get() != null) {
        block()
    } else {
        transaction(context) {
            block()
        }
    }
}

