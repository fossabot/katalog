package com.bol.katalog.api

import com.bol.katalog.testing.ref
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@ExtendWith(SpringExtension::class)
@WebFluxTest(PagingSortingController::class)
@WithMockUser
class PagingSortingTest {
    @Autowired
    private lateinit var client: WebTestClient

    @Test
    fun `Can get with default pagination`() {
        expectThat(get(PaginationRequest(), SortingRequest())).isEqualTo(
            PageResponse(
                data = itemListOf(1..25),
                totalElements = 500,
                totalPages = 20
            )
        )
    }

    @Test
    fun `Can get pages`() {
        expectThat(get(PaginationRequest(1, 2), SortingRequest())).isEqualTo(
            PageResponse(
                data = itemListOf(1..2),
                totalElements = 500,
                totalPages = 250
            )
        )
        expectThat(get(PaginationRequest(2, 2), SortingRequest())).isEqualTo(
            PageResponse(
                data = itemListOf(3..4),
                totalElements = 500,
                totalPages = 250
            )
        )
    }

    @Test
    fun `Can get pages with max page size`() {
        expectThat(get(PaginationRequest(1, 500), SortingRequest())).isEqualTo(
            PageResponse(
                data = itemListOf(1..25),
                totalElements = 500,
                totalPages = 20
            )
        )
    }

    @Test
    fun `Can sort on fields`() {
        expectThat(
            get(
                PaginationRequest(),
                SortingRequest("positive", SortDirection.ASC)
            ).data
        )
            .isEqualTo(itemListOf(1..25))

        expectThat(
            get(
                PaginationRequest(),
                SortingRequest("negative", SortDirection.ASC)
            ).data
        )
            .isEqualTo(itemListOf(500 downTo 476))
    }

    @Test
    fun `Can sort descending on fields`() {
        expectThat(
            get(
                PaginationRequest(),
                SortingRequest("positive", SortDirection.DESC)
            ).data
        )
            .isEqualTo(itemListOf(500 downTo 476))

        expectThat(
            get(
                PaginationRequest(),
                SortingRequest("negative", SortDirection.DESC)
            ).data
        )
            .isEqualTo(itemListOf(1..25))
    }

    fun get(pagination: PaginationRequest, sorting: SortingRequest): PageResponse<Item> {
        var result: PageResponse<Item>? = null

        client.get()
            .uri {
                it
                    .path("/test")
                    .queryParam("page", pagination.page)
                    .queryParam("size", pagination.size)
                    .queryParam("sortColumn", sorting.sortColumn)
                    .queryParam("sortDirection", sorting.sortDirection)
                    .build()
            }
            .exchange()
            .expectStatus().isOk
            .expectBody(ref<PageResponse<Item>>())
            .consumeWith<Nothing> {
                result = it.responseBody
            }

        return result!!
    }
}

@RestController
class PagingSortingController {
    @GetMapping("/test")
    fun get(
        pagination: PaginationRequest,
        sorting: SortingRequest
    ): PageResponse<Item> {
        var data: Sequence<Item> =
            itemListOf(1..500).asSequence()

        data = data.sort(sorting) { column ->
            when (column) {
                "positive" -> {
                    { it.positive }
                }
                "negative" -> {
                    { it.negative }
                }
                else -> {
                    { it.positive }
                }
            }
        }

        return runBlocking { data.paginate(pagination) }
    }
}

data class Item(val positive: Int, val negative: Int)

private fun toItem(value: Int) = Item(value, -value)

private fun itemListOf(range: IntProgression): Collection<Item> {
    return range.toList().map {
        toItem(it)
    }
}