package com.bol.katalog.security

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.core.context.ReactiveSecurityContextHolder

suspend fun <T> CoroutineScope.withUserDetails(block: suspend CoroutineScope.() -> T): T {
    val userDetails = ReactiveSecurityContextHolder
        .getContext()
        .awaitFirstOrNull()
        ?.authentication
        ?.principal as KatalogUserDetails

    return withUserDetails(userDetails, block)
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