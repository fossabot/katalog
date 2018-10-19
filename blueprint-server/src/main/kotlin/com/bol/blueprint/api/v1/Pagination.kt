package com.bol.blueprint.api.v1

import kotlin.math.ceil

data class Page<T>(val data: Collection<T>, val totalElements: Int, val totalPages: Int)

data class PaginationRequest(val page: Int?, val size: Int?)

fun <T> Collection<T>.paginate(pagination: PaginationRequest?, maxItemsPerPage: Int): Page<T> = paginate(pagination, maxItemsPerPage) { it -> it }

fun <T, R> Collection<T>.paginate(pagination: PaginationRequest?, maxItemsPerPage: Int, mapFunction: (T) -> R): Page<R> {
    val pageSize = minOf(pagination?.size ?: Int.MAX_VALUE, maxItemsPerPage)

    // Calculate the number of items to skip
    val drop = if (pagination?.page != null && pagination.size != null) {
        (pagination.page - 1) * pagination.size
    } else 0

    val items = this
            .drop(drop)
            .take(pageSize)
            .toList()

    return Page(data = items.map { mapFunction(it) }, totalElements = this.count(), totalPages = ceil(this.count().toDouble() / pageSize.toDouble()).toInt())
}