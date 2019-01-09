package com.bol.katalog.security

import com.bol.katalog.domain.Event

data class GroupCreatedEvent(val id: GroupId, val name: String) : Event()

data class UserAddedToGroupEvent(val userId: UserId, val groupId: GroupId, val permissions: List<GroupPermission>) :
    Event()