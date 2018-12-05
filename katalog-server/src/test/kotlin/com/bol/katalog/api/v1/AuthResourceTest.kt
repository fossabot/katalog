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
import strikt.assertions.isEqualTo

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WithUserDetails
class AuthResourceTest : AbstractResourceTest() {
    private val baseUrl = "/api/v1/auth"

    @Test
    fun `Can get logged in user`() {
        val result = client.get().uri("$baseUrl/user-details").exchange()
            .expectStatus().isOk
            .expectBody(ref<AuthResource.User>())
            .returnResult()

        expectThat(result.responseBody!!).isEqualTo(
            AuthResource.User(
                username = "user",
                enabled = true,
                authorities = listOf("ROLE_USER"),
                groups = listOf(
                    UserGroup(Group("group1"), allPermissions()),
                    UserGroup(Group("group2"), allPermissions())
                )
            )
        )
    }
}