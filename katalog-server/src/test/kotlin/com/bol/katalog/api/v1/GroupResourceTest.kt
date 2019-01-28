package com.bol.katalog.api.v1

import com.bol.katalog.security.WithKatalogUser
import com.bol.katalog.security.allPermissions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

@WebFluxTest(GroupResource::class)
@WithKatalogUser("user1")
class GroupResourceTest : AbstractResourceTest() {
    override fun getBaseUrl() = "/api/v1/groups"

    @Test
    @WithKatalogUser("user1")
    fun `Can get available groups for user1`() {
        val result = exchange<Collection<GroupResource.GroupResponse>>()
        expectThat(result!!).containsExactly(
            GroupResource.GroupResponse(
                "id-group1",
                "group1",
                allPermissions()
            )
        )
    }

    @Test
    @WithKatalogUser("user-no-groups")
    fun `Can get available groups for user-no-groups`() {
        val result = exchange<Collection<GroupResource.GroupResponse>>()
        expectThat(result!!).isEmpty()
    }

    @Test
    @WithKatalogUser("admin")
    fun `Can get available groups for admin`() {
        val result = exchange<Collection<GroupResource.GroupResponse>>()
        expectThat(result!!).containsExactly(
            GroupResource.GroupResponse(
                "id-group1",
                "group1",
                allPermissions()
            )
        )
    }
}