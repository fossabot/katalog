package com.bol.katalog.security

import com.bol.katalog.users.GroupPermission
import com.bol.katalog.users.UserId

data class Group(val id: GroupId, val name: String, val members: List<GroupMember>)

typealias GroupId = String

data class GroupMember(val userId: UserId, val permissions: Collection<GroupPermission>)

fun allPermissions() = setOf(
    GroupPermission.CREATE,
    GroupPermission.READ,
    GroupPermission.UPDATE,
    GroupPermission.DELETE
)