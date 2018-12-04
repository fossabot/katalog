package com.bol.katalog

import com.bol.katalog.security.CoroutineUserContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

fun <T> withTestUser(block: suspend CoroutineScope.() -> T) {
    runBlocking {
        try {
            CoroutineUserContext.set(TestUsers.user())
            block()
        } finally {
            CoroutineUserContext.set(null)
        }
    }
}
