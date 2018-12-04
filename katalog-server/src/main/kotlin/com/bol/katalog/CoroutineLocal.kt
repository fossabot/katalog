package com.bol.katalog

import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

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