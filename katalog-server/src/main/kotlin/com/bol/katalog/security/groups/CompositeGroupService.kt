package com.bol.katalog.security.groups

import com.bol.katalog.domain.Group
import com.bol.katalog.domain.UserGroup
import com.bol.katalog.security.KatalogUserDetails

class CompositeGroupService(private val children: List<GroupProvider>) : GroupService {
    override suspend fun getAvailableGroups(): Collection<Group> {
        val result = mutableSetOf<Group>()
        children.forEach { result += it.getAvailableGroups() }
        return result
    }

    override suspend fun getUserGroups(user: KatalogUserDetails): Collection<UserGroup> {
        val result = mutableSetOf<UserGroup>()
        children.forEach { result += it.getUserGroups(user) }
        return result
    }
}