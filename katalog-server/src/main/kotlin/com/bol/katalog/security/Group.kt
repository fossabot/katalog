package com.bol.katalog.security

import com.bol.katalog.users.GroupPermission
import com.bol.katalog.users.UserId
import com.fasterxml.jackson.annotation.JsonValue

data class Group(val id: GroupId, val name: String, val members: List<GroupMember>)

data class GroupId(@get:JsonValue val value: String)

data class GroupMember(val userId: UserId, val permissions: Collection<GroupPermission>)

fun allPermissions() = setOf(
    GroupPermission.CREATE,
    GroupPermission.READ,
    GroupPermission.UPDATE,
    GroupPermission.DELETE
)