package com.bol.blueprint.api.v1

import com.bol.blueprint.api.AbstractResourceTest
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.junit4.SpringRunner
import strikt.api.expectThat
import strikt.assertions.contentEquals

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WithUserDetails
class RepositoryResourceTest : AbstractResourceTest() {
    private val baseUrl = "/api/v1/repository"

    @Test
    fun `Can get artifact`() {
        val result = client.get().uri("$baseUrl/ns1/schema1/1.0.0/artifact1.json").exchange()
            .expectStatus().isOk
            .expectBody(ref<ByteArray>())
            .returnResult()

        expectThat(result.responseBody).contentEquals(byteArrayOf(1, 2, 3))
    }
}