package com.bol.katalog.cqrs.hazelcast

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.Event
import com.hazelcast.transaction.TransactionContext
import com.hazelcast.transaction.TransactionOptions
import com.hazelcast.transaction.TransactionalTaskContext

class Transaction(private val context: AggregateContext) {
    private val tx: TransactionContext

    init {
        val txOptions = TransactionOptions()
        txOptions.transactionType = TransactionOptions.TransactionType.ONE_PHASE
        tx = context.newTransactionContext(txOptions)
        tx.beginTransaction()
    }

    fun hazelcastTx(): TransactionalTaskContext = tx

    private val events = mutableListOf<Event>()

    fun event(event: Event) {
        events += event
    }

    suspend fun require(command: Command) {
        context.handleCommand(command)
    }

    suspend fun commit() {
        events.forEach { event ->
            context.publish(event)
        }
        tx.commitTransaction()
    }

    fun rollback() {
        tx.rollbackTransaction()
    }
}