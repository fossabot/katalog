package com.bol.blueprint.api.v1

import com.bol.blueprint.api.AbstractResourceTest
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PaginationTest.PaginationController::class)
class PaginationTest : AbstractResourceTest() {
    @Test
    fun `Can get with default pagination`() {
        get().isEqualTo<Nothing>(Page(data = listOf(1, 2, 3), totalElements = 5, totalPages = 2))
    }

    @Test
    fun `Can get pages`() {
        get(PaginationRequest(1, 2)).isEqualTo<Nothing>(Page(data = listOf(1, 2), totalElements = 5, totalPages = 3))
        get(PaginationRequest(2, 2)).isEqualTo<Nothing>(Page(data = listOf(3, 4), totalElements = 5, totalPages = 3))
        get(PaginationRequest(3, 2)).isEqualTo<Nothing>(Page(data = listOf(5), totalElements = 5, totalPages = 3))
    }

    @Test
    fun `Can get pages with max page size`() {
        get(PaginationRequest(1, 5)).isEqualTo<Nothing>(Page(data = listOf(1, 2, 3), totalElements = 5, totalPages = 2))
    }

    fun get(pagination: PaginationRequest? = null) =
            client.get()
                    .uri {
                        it
                                .path("/test")
                                .queryParam("page", pagination?.page ?: "")
                                .queryParam("size", pagination?.size ?: "")
                                .build()
                    }
                    .exchange()
                    .expectBody(ref<Page<Int>>())

    @RestController
    @RequestMapping("/test")
    class PaginationController {
        @GetMapping
        fun get(pagination: PaginationRequest?): Page<Int> {
            return listOf(1, 2, 3, 4, 5).paginate(pagination, 3)
        }
    }
}