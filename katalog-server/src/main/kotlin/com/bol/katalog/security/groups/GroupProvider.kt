package com.bol.katalog.security.groups

import com.bol.katalog.domain.Group
import com.bol.katalog.domain.UserGroup
import com.bol.katalog.security.KatalogUserDetails

interface GroupProvider {
    suspend fun getAvailableGroups(): Collection<Group>

    suspend fun getUserGroups(user: KatalogUserDetails): Collection<UserGroup>
}