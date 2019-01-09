package com.bol.katalog.api.v1

import com.bol.katalog.api.AbstractResourceTest
import com.bol.katalog.security.GroupPermission
import com.bol.katalog.security.allPermissions
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
    @WithUserDetails("user1")
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
                listOf(GroupPermission.READ)
            )
        )
    }

    @Test
    @WithUserDetails("user2")
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
                listOf(GroupPermission.READ)
            )
        )
    }

    @Test
    @WithUserDetails("admin")
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