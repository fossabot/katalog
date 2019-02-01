package com.bol.katalog.plugin.hazelcast

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future

fun <T> Future<T>.asCompletableFuture(): CompletableFuture<T> {
    return CompletableFuture.supplyAsync {
        try {
            this@asCompletableFuture.get()
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } catch (e: ExecutionException) {
            throw RuntimeException(e)
        }
    }
}