package com.bol.blueprint

import com.bol.blueprint.domain.BlueprintUserDetails
import com.bol.blueprint.domain.CurrentUserSupplier
import com.bol.blueprint.domain.Group
import org.springframework.security.core.authority.SimpleGrantedAuthority

class FakeUserDetailsSupplier(private val user: BlueprintUserDetails) : CurrentUserSupplier {
    override suspend fun getCurrentUser() = user
}

object FakeUsers {
    fun testUser() = FakeUserDetailsSupplier(
            BlueprintUserDetails(
                    "user",
                    "password",
                    listOf(SimpleGrantedAuthority("ROLE_USER")),
                    listOf(Group("groupA"), Group("groupB"))
            )
    )
}