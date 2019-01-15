package com.bol.katalog.security

import com.bol.katalog.cqrs.Event
import com.bol.katalog.users.GroupPermission
import com.bol.katalog.users.UserId
import org.springframework.security.core.GrantedAuthority

data class GroupCreatedEvent(val id: GroupId, val name: String) : Event()

data class GroupDisabledEvent(val id: GroupId) : Event()

data class UserCreatedEvent(
    val id: UserId,
    val username: String,
    val encodedPassword: String?,
    val authorities: Set<GrantedAuthority>
) : Event()

data class UserAddedToGroupEvent(val userId: UserId, val groupId: GroupId, val permissions: Set<GroupPermission>) :
    Event()

data class UserRemovedFromGroupEvent(val userId: UserId, val groupId: GroupId) : Event()

data class UserDisabledEvent(val id: UserId) : Event()