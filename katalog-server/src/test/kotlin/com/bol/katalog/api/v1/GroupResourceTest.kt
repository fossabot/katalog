package com.bol.katalog.api.v1

import com.bol.katalog.api.AbstractResourceTest
import com.bol.katalog.domain.Group
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.junit4.SpringRunner
import strikt.api.expectThat
import strikt.assertions.containsExactly

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GroupResourceTest : AbstractResourceTest() {
    private val baseUrl = "/api/v1/groups"

    @Test
    @WithUserDetails
    fun `Can get available groups for user`() {
        val result = client.get().uri(baseUrl).exchange()
            .expectStatus().isOk
            .expectBody(ref<Collection<Group>>())
            .returnResult()

        expectThat(result.responseBody!!).containsExactly(
            Group("group1"), Group("group2")
        )
    }

    @Test
    @WithUserDetails("user2")
    fun `Can get available groups for user2`() {
        val result = client.get().uri(baseUrl).exchange()
            .expectStatus().isOk
            .expectBody(ref<Collection<Group>>())
            .returnResult()

        expectThat(result.responseBody!!).containsExactly(
            Group("group2"), Group("group3")
        )
    }

    @Test
    @WithUserDetails("admin")
    fun `Can get available groups for admin`() {
        val result = client.get().uri(baseUrl).exchange()
            .expectStatus().isOk
            .expectBody(ref<Collection<Group>>())
            .returnResult()

        expectThat(result.responseBody!!).containsExactly(
            Group("group1"), Group("group2"), Group("group3"), Group("administrators")
        )
    }
}