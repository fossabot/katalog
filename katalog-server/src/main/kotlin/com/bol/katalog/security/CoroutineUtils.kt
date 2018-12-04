package com.bol.katalog.security

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import reactor.core.publisher.Mono

fun <T> monoWithUserDetails(userDetails: KatalogUserDetails, block: suspend CoroutineScope.() -> T): Mono<T> {
    return GlobalScope.mono {
        try {
            CoroutineUserContext.set(userDetails)
            block()
        } finally {
            CoroutineUserContext.set(null)
        }
    }
}

fun <T> runBlockingWithUserDetails(userDetails: KatalogUserDetails, block: suspend CoroutineScope.() -> T): T {
    return runBlocking {
        try {
            CoroutineUserContext.set(userDetails)
            block()
        } finally {
            CoroutineUserContext.set(null)
        }
    }
}
