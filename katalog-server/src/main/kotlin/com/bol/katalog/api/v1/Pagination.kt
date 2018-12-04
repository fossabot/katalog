package com.bol.katalog.api.v1

import kotlin.math.ceil

data class PageResponse<T>(val data: Collection<T>, val totalElements: Int, val totalPages: Int)

data class PaginationRequest(val page: Int = 1, val size: Int = 25)

suspend fun <T> Collection<T>.paginate(pagination: PaginationRequest): PageResponse<T> =
    paginate(pagination) { it -> it }

suspend fun <T, R> Collection<T>.paginate(
    pagination: PaginationRequest,
    mapFunction: suspend (T) -> R
): PageResponse<R> {
    val size = minOf(pagination.size, 25)

    // Calculate the number of items to skip
    val drop = (pagination.page - 1) * size

    val items = this
        .drop(drop)
        .take(size)
        .toList()

    return PageResponse(
        data = items.map { mapFunction(it) },
        totalElements = this.count(),
        totalPages = ceil(this.count().toDouble() / size.toDouble()).toInt()
    )
}