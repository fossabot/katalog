package com.bol.katalog.utils

import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * Allows a value to be set that can be accessed by another piece of code in the same coroutine.
 * See: https://github.com/Kotlin/kotlinx.coroutines/issues/119
 */
class CoroutineLocal<T> {
    val values = WeakHashMap<CoroutineContext, T>()

    suspend fun get(): T? =
        values[coroutineContext]

    suspend fun set(value: T?) {
        if (value == null)
            values.remove(coroutineContext)
        else
            values[coroutineContext] = value
    }

}