package com.bol.katalog.security

import com.bol.katalog.users.UserId
import com.bol.katalog.utils.CoroutineLocal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.reactor.mono
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import reactor.core.publisher.Mono

object CoroutineUserIdContext {
    private val currentUser = CoroutineLocal<UserId>()

    suspend fun get() = currentUser.get()
    suspend fun set(user: UserId?) {
        this.currentUser.set(user)
    }

    suspend fun <T> with(user: UserId?, block: suspend () -> T): T {
        val previous = get()
        set(user)
        try {
            return block()
        } finally {
            set(previous)
        }
    }
}

@UseExperimental(ExperimentalCoroutinesApi::class)
fun <T> monoWithUserId(block: suspend HasUserIdBlock.() -> T): Mono<T> {
    return ReactiveSecurityContextHolder
        .getContext()
        .flatMap { details ->
            val userDetails = details?.authentication?.principal as KatalogUserDetails?
            GlobalScope.mono(Dispatchers.Unconfined) { withUserId(userDetails?.getUser()?.id, block) }
        }
}

suspend fun <T> withUserId(
    userId: UserId?,
    block: suspend HasUserIdBlock.() -> T
): T {
    try {
        CoroutineUserIdContext.set(userId)
        return block(HasUserIdBlock(userId))
    } finally {
        CoroutineUserIdContext.set(null)
    }
}

class HasUserIdBlock(val userId: UserId?)