package com.bol.katalog.api.v1

import com.bol.katalog.api.AbstractResourceTest
import com.bol.katalog.domain.Group
import com.bol.katalog.domain.UserGroup
import com.bol.katalog.domain.allPermissions
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
            .expectBody(ref<Collection<UserGroup>>())
            .returnResult()

        expectThat(result.responseBody!!).containsExactly(
            UserGroup(Group("group1"), allPermissions()),
            UserGroup(Group("group2"), allPermissions())
        )
    }

    @Test
    @WithUserDetails("user2")
    fun `Can get available groups for user2`() {
        val result = client.get().uri(baseUrl).exchange()
            .expectStatus().isOk
            .expectBody(ref<Collection<UserGroup>>())
            .returnResult()

        expectThat(result.responseBody!!).containsExactly(
            UserGroup(Group("group2"), allPermissions()),
            UserGroup(Group("group3"), allPermissions())
        )
    }

    @Test
    @WithUserDetails("admin")
    fun `Can get available groups for admin`() {
        val result = client.get().uri(baseUrl).exchange()
            .expectStatus().isOk
            .expectBody(ref<Collection<UserGroup>>())
            .returnResult()

        expectThat(result.responseBody!!).containsExactly(
            UserGroup(Group("group1"), allPermissions()),
            UserGroup(Group("group2"), allPermissions()),
            UserGroup(Group("group3"), allPermissions())
        )
    }
}