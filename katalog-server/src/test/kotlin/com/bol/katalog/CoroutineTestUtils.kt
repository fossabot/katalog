package com.bol.katalog

import com.bol.katalog.security.CoroutineUserContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

fun <T> withTestUser1(block: suspend CoroutineScope.() -> T) {
    runBlocking {
        try {
            CoroutineUserContext.set(TestUsers.user1())
            block()
        } finally {
            CoroutineUserContext.set(null)
        }
    }
}
