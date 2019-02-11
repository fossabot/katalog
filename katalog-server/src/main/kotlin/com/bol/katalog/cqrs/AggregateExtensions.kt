package com.bol.katalog.cqrs

import com.bol.katalog.cqrs.hazelcast.HazelcastAggregate
import com.bol.katalog.security.CoroutineUserIdContext

suspend fun HazelcastAggregate.send(c: Command) {
    val user =
        CoroutineUserIdContext.get() ?: throw IllegalStateException("Trying to send a command without a user!")
    sendAs(user, c)
}
