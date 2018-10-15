package com.bol.blueprint.api.v1

data class Page<T>(val data: Collection<T>, val total: Int)

data class PaginationRequest(val page: Int?, val size: Int?)

fun <T> Sequence<T>.paginate(pagination: PaginationRequest?, maxSize: Int = 25): Page<T> {
    // Calculate the number of items to skip
    val drop = ((pagination?.page?.minus(1)) ?: 0) * (pagination?.size ?: 0)

    val items = this
            .drop(drop)
            .take(minOf(pagination?.size ?: Int.MAX_VALUE, maxSize))
            .toList()

    return Page(data = items, total = this.count())
}