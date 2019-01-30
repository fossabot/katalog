package com.bol.katalog.utils

import com.bol.katalog.security.CoroutineUserIdContext
import com.bol.katalog.security.SystemUser
import com.bol.katalog.users.UserId
import kotlinx.coroutines.runBlocking

fun <T> runBlockingAs(userId: UserId, block: suspend () -> T) = runBlocking {
    CoroutineUserIdContext.set(userId)
    block()
}

fun <T> runBlockingAsSystem(block: suspend () -> T) = runBlockingAs(SystemUser.get().id, block)