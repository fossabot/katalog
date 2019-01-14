package com.bol.katalog.security

import com.bol.katalog.utils.CoroutineLocal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.reactor.mono
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import reactor.core.publisher.Mono

object CoroutineUserContext {
    private val currentUser = CoroutineLocal<User>()

    suspend fun get() = currentUser.get()
    suspend fun set(user: User?) {
        this.currentUser.set(user)
    }
}

fun <T> monoWithUserDetails(block: suspend CoroutineScope.() -> T): Mono<T> {
    return ReactiveSecurityContextHolder
        .getContext()
        .flatMap { details ->
            val userDetails = details?.authentication?.principal as KatalogUserDetails?
            GlobalScope.mono { withUserDetails(userDetails?.getUser(), block) }
        }
}

suspend fun <T> CoroutineScope.withUserDetails(
    userDetails: User?,
    block: suspend CoroutineScope.() -> T
): T {
    try {
        CoroutineUserContext.set(userDetails)
        return block()
    } finally {
        CoroutineUserContext.set(null)
    }
}
