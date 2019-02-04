package com.bol.katalog.api

enum class SortDirection {
    ASC, DESC
}

data class SortingRequest(val sortColumn: String? = null, val sortDirection: SortDirection = SortDirection.ASC)

inline fun <T> Sequence<T>.sort(
    request: SortingRequest,
    crossinline selectorFn: (String?) -> (T) -> Comparable<*>
): Sequence<T> {
    val selector = selectorFn(request.sortColumn)

    val comparator = when (request.sortDirection) {
        SortDirection.ASC -> compareBy(selector)
        SortDirection.DESC -> compareByDescending(selector)
    }
    return this.sortedWith(comparator)
}