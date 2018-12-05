package com.bol.katalog.security

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.reactor.mono
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import reactor.core.publisher.Mono

fun <T> monoWithUserDetails(block: suspend CoroutineScope.() -> T): Mono<T> {
    return ReactiveSecurityContextHolder
        .getContext()
        .flatMap { details ->
            val userDetails = details?.authentication?.principal as KatalogUserDetails?
            GlobalScope.mono { withUserDetails(userDetails, block) }
        }
}

suspend fun <T> CoroutineScope.withUserDetails(
    userDetails: KatalogUserDetails?,
    block: suspend CoroutineScope.() -> T
): T {
    try {
        CoroutineUserContext.set(userDetails)
        return block()
    } finally {
        CoroutineUserContext.set(null)
    }
}
