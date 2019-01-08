package com.bol.katalog.security.tokens

import com.bol.katalog.domain.Group
import com.bol.katalog.security.KatalogUserDetails
import com.bol.katalog.security.groups.GroupProvider

// Group provider that allows the user's groups to be extracted from the JWT token
class JwtGroupProvider : GroupProvider {
    override suspend fun getAvailableGroups(): Collection<Group> = emptyList()

    override suspend fun getUserGroups(user: KatalogUserDetails) = when (user) {
        is JwtTokenService.BearerUserDetails -> user.groups
        else -> emptyList()
    }
}