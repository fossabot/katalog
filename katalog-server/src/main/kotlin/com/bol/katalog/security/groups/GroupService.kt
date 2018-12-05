package com.bol.katalog.security.groups

import com.bol.katalog.domain.Group

interface GroupService {
    suspend fun getAvailableGroups(): Collection<Group>
}