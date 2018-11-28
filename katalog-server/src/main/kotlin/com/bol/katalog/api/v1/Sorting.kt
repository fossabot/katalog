package com.bol.katalog.api.v1

enum class SortDirection {
    ASC, DESC
}

data class SortingRequest(val sortColumn: String? = null, val sortDirection: SortDirection = SortDirection.ASC)

inline fun <T> Collection<T>.sort(
    request: SortingRequest,
    crossinline selectorFn: (String?) -> (T) -> Comparable<*>
): Collection<T> {
    val selector = selectorFn(request.sortColumn)

    val comparator = when (request.sortDirection) {
        SortDirection.ASC -> compareBy(selector)
        SortDirection.DESC -> compareByDescending(selector)
    }
    return this.sortedWith(comparator)
}