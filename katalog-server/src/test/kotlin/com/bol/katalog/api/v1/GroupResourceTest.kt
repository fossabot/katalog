package com.bol.katalog.api.v1

import com.bol.katalog.AbstractSpringTest
import com.bol.katalog.security.WithKatalogUser
import com.bol.katalog.security.allPermissions
import com.bol.katalog.users.GroupPermission
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly

class GroupResourceTest : AbstractSpringTest() {
    private val baseUrl = "/api/v1/groups"

    @Test
    @WithKatalogUser("user1")
    fun `Can get available groups for user1`() {
        val result = client.get().uri(baseUrl).exchange()
            .expectStatus().isOk
            .expectBody(ref<Collection<GroupResource.GroupResponse>>())
            .returnResult()

        expectThat(result.responseBody!!).containsExactly(
            GroupResource.GroupResponse(
                "id-group1",
                "group1",
                allPermissions()
            ),
            GroupResource.GroupResponse(
                "id-group2",
                "group2",
                setOf(GroupPermission.READ)
            )
        )
    }

    @Test
    @WithKatalogUser("user2")
    fun `Can get available groups for user2`() {
        val result = client.get().uri(baseUrl).exchange()
            .expectStatus().isOk
            .expectBody(ref<Collection<GroupResource.GroupResponse>>())
            .returnResult()

        expectThat(result.responseBody!!).containsExactly(
            GroupResource.GroupResponse(
                "id-group2",
                "group2",
                allPermissions()
            ),
            GroupResource.GroupResponse(
                "id-group3",
                "group3",
                setOf(GroupPermission.READ)
            )
        )
    }

    @Test
    @WithKatalogUser("admin")
    fun `Can get available groups for admin`() {
        val result = client.get().uri(baseUrl).exchange()
            .expectStatus().isOk
            .expectBody(ref<Collection<GroupResource.GroupResponse>>())
            .returnResult()

        expectThat(result.responseBody!!).containsExactly(
            GroupResource.GroupResponse(
                "id-group1",
                "group1",
                allPermissions()
            ),
            GroupResource.GroupResponse(
                "id-group2",
                "group2",
                allPermissions()
            ),
            GroupResource.GroupResponse(
                "id-group3",
                "group3",
                allPermissions()
            )
        )
    }
}