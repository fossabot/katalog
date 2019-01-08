package com.bol.katalog.security.groups

import com.bol.katalog.domain.Group
import com.bol.katalog.domain.GroupPermission
import com.bol.katalog.domain.UserGroup
import com.bol.katalog.security.KatalogUserDetails

interface GroupService {
    suspend fun getAvailableGroups(): Collection<Group>

    suspend fun getUserGroups(user: KatalogUserDetails): Collection<UserGroup>

    suspend fun hasGroupPermission(user: KatalogUserDetails, group: Group, permission: GroupPermission) = when {
        user.isAdmin() -> true
        getUserGroups(user).any { it.group == group && it.permissions.contains(permission) } -> true
        else -> false
    }

}